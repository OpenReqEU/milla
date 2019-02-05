package eu.openreq.milla.controllers;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import eu.openreq.milla.services.MallikasService;
import eu.openreq.milla.services.JSONParser;
import io.swagger.annotations.ApiOperation; 
//import io.swagger.annotations.*;
import eu.openreq.milla.models.json.*;

@SpringBootApplication
@RestController
public class DetectionController {

	@Value("${milla.mallikasAddress}")
	private String mallikasAddress;

	@Value("${milla.upcSimilarityAddress}")
	private String upcSimilarityAddress;
	
	@Value("${milla.upcCrossReferrenceAddress}")
	private String upcCrossReferenceAddress;

	@Autowired
	MallikasService mallikasService;
	
	@Autowired
	MillaController millaController;

	/**
	 * Post a Collection of OpenReq JSON Requirements to UPC for Similarity
	 * detection.
	 * 
	 * @param projectId
	 * @return ResponseEntity<?>
	 * @throws IOException
	 */
	@ApiOperation(value = "Cache requirements for UPC dependency Detection", 
			notes = "<b>Functionality</b>: Post all requirements and dependencies in a project as a String to UPC services in order to be cached for dependency detection purposes. <br>"
					+ "<b>Precondition</b>: The project has been cached in Mallikas.<br>"
					+ "<b>Postcondition</b>: After successfully caching requirements in UPC service, similarity detection can be carried out.<br>"
					+ "<b>Exception</b>: Not needed for DKPro."
					+ "<br><b>Prarameter: </b>"
					+ "<br>projectId: The project id in Mallikas (e.g., QTWB).")
	@ResponseBody
	@PostMapping(value = "detectSimilarityAddReqs")
	public ResponseEntity<?> postRequirementsToUPCSimilarityDetection(@RequestBody String projectId)
			throws IOException {

		RestTemplate rt = new RestTemplate();

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		String requirements = mallikasService.getAllRequirementsInProjectFromMallikas(projectId,
				mallikasAddress + "projectRequirements");

		String completeAddress = upcSimilarityAddress + "upc/similarity-detection/DB/AddReqs";
		
		HttpEntity<String> entity = new HttpEntity<String>(requirements, headers);

		ResponseEntity<?> response = null;

		try {
			response = rt.postForEntity(completeAddress, entity, String.class);

		} catch (HttpClientErrorException e) {
			return new ResponseEntity<>("UPC error:\n\n" + e.getResponseBodyAsString(), e.getStatusCode());
		}
		return response;
	}

	/**
	 * Post a Collection of OpenReq JSON Requirements and Dependencies to UPC for
	 * comparing two requirements.
	 * @param projectId
	 * @param reqId1
	 * @param reqId2
	 * @param component
	 * @return
	 * @throws IOException
	 */
	@ApiOperation(value = "Detect similarity between two requirements using UPC Similarity Detection",  
	notes = "<b>Functionality</b>: All requirements of a given project are posted to UPC Similarity Detection in order to detect similarity between two specified requirements to each other."
	+ "<br><b>Precondition</b>: The project has been cached in Mallikas."
	+ "<br><b>Postcondition</b>: After successfully detection, the potially detected similarity, given that it is above the treshold, is stored in Mallikas using the similarity dependency type and proposed status."
	+ "<br><b>Notes: This is inefficient method since entire project is fetched from mallikas</b> ."
	+ "<br><b>Parameters:</b>"
	+ "<br>compare: The fields that are taken into accoung in comparison (Name-Text-Comments-All)."
	+ "<br>component: The component or algorithm used for comparison (e.g. DKPro)."
	+ "<br>projectId: The project id in Mallikas (e.g., QTWB)."
	+ "<br>reqId1: The id of the requirement that is compared to other requirement (reqId2) in the project."
	+ "<br>reqId2: The id of the requirement that is compared to other requirement (reqId1) in the project.")
	@ResponseBody
	@PostMapping(value = "detectSimilarityReqReq")
	public ResponseEntity<?> postRequirementsToUPCSimilarityDetectionReqReq(@RequestParam String compare, @RequestParam String projectId,
			@RequestParam String reqId1, @RequestParam String reqId2, @RequestParam String component)
			throws IOException{

		String completeAddress = upcSimilarityAddress + "upc/similarity-detection/ReqReq?compare=" +compare+ "&component=" + component +"req1=" + reqId1 + "&req2=" + reqId2;

		ResponseEntity<?> entity = receiveDependenciesAndSendToMallikas(projectId, completeAddress);
		return entity;
	}
	
	/**
	 * 
	 * @param component
	 * @param elements
	 * @param projectId
	 * @param reqId
	 * @param threshold
	 * @return
	 * @throws IOException
	 */
	@ApiOperation(value = "Detect similarity of one requirement againsta all other requirements of a project using UPC Similarity Detection",  
			notes = "<b>Functionality</b>: All requirements of a given project are posted to UPC Similarity Detection in order to detect similarity between one specified requirements in to project to all other requirements. "
			+ "<br><b>Precondition</b>: The project has been cached in Mallikas. For other than DKPro, the project needs to be cache in UPC, see \"detectSimilarityAddReqs\""
			+ "<br><b>Postcondition</b>: After successfully detection, the detected new similarities are stored in Mallikas using the similarity dependency type and proposed status."
			+ "<br><b>Parameters:</b>"
			+ "<br>compare: what fields are taken into accoung in comparison (Name-Text-Comments-All)."
			+ "<br>component: The component or algorithm used for comparison (e.g. DKPro)."
			+ "<br>elements: the maximum number of detected dependencies (e.g. 5)."
			+ "<br>projectId: The project id in Mallikas."
			+ "<br>reqId: The id of the requirement that is compared to other requirements in the project."
			+ "<br>threshold: The minimum score for similarity detection (e.g. 0.3).")
@ResponseBody
	@PostMapping(value = "detectSimilarityReqProject")
	public ResponseEntity<?> postRequirementsToUPCSimilarityDetectionReqProject(@RequestParam String compare, @RequestParam String component, @RequestParam String elements, @RequestParam String projectId, @RequestParam String reqId, @RequestParam String threshold)
			throws IOException{

		String completeAddress = upcSimilarityAddress + "upc/similarity-detection/ReqProject?compare=" + compare +"&component=" + component + "&num_elements="+elements + "&project="+ projectId  + "&req=" + reqId+ "&threshold="+threshold;

		ResponseEntity<?> entity = receiveDependenciesAndSendToMallikas(projectId, completeAddress);
		return entity;
	}

	/**
	 * Post a Collection of OpenReq JSON Requirements and Dependencies in a project to UPC for Similarity
	 * detection.
	 * 
	 * @param projectId
	 * @param component
	 * @param threshold
	 * @param elements
	 * @return
	 * @throws IOException
	 */
	@ApiOperation(value = "Detect similarity between all requirements of a project using UPC Similarity Detection", 
			notes = "<b>Functionality</b>: All requirements of a given project are posted  to UPC Similarity Detection in order to detect similarity between all requirements. "
					+ "<br><b>Precondition</b>: The project has been cached in Mallikas."
					+ "<br><b>Postcondition</b>: After successfully detection, the detected new similarities are stored in Mallikas using the similarity dependency type and proposed status."
					+ "<br><b>Parameters:</b>"
					+ "<br>compare: What fields of a requirement are taken into accoung in comparison (Name-Text-Comments-All)."
					+ "<br>component: The component or algorithm used for comparison (e.g. DKPro)."
					+ "<br>elements: The maximum number of detected dependencies (e.g. 5)."
					+ "<br>projectId: The project id in Mallikas."
					+ "<br>threshold: The minimum score for similarity (e.g. 0.3).")
	@ResponseBody
	@PostMapping(value = "detectSimilarityProject")
	public ResponseEntity<?> postRequirementsToUPCSimilarityDetectionProject(@RequestParam String compare, @RequestParam String component, @RequestParam String elements, @RequestParam String projectId, @RequestParam String threshold)
			throws IOException {
		String completeAddress = upcSimilarityAddress
				+ "upc/similarity-detection/Project?compare=" + compare +"&component="  + component + "&num_elements="+elements + "&project="+ projectId  + "&threshold="+threshold;
		ResponseEntity<?> entity = receiveDependenciesAndSendToMallikas(projectId, completeAddress);
		return entity;
	}
	
	
	
	/**
	 * Post a Collection of OpenReq JSON Requirements and Dependencies in a project to UPC for Cross-Reference
	 * detection.
	 * 
	 * @param projectId
	 * @return ResponseEntity<?>
	 * @throws IOException
	 */
	@ApiOperation(value = "Detect cross-references in all requirements of a project using UPC Cross-Reference Detection", 
			notes = "<b>Functionality</b>: Post all requirements and dependencies in a project as a String to UPC for Cross-Reference Detection. Requires projectId <br>"
					+ "<b>Precondition</b>: The project has been cached in Mallikas.<br>"
					+ "<b>Postcondition</b>: After successfully detection, detected cross references are stored in Mallikas as proposed dependencies."
					+ "<br><b>Prarameter: </b>"
					+ "<br>projectId: The project id in Mallikas (e.g., QTWB).")
	@ResponseBody
	@PostMapping(value = "detectCrossReferenceProject")
	public ResponseEntity<?> postRequirementsToUPCCrossReferenceDetectionProject(@RequestParam String projectId )
			throws IOException {

		String completeAddress = upcCrossReferenceAddress
				+ "upc/cross-reference-detection/json/"+ projectId;

		ResponseEntity<?> entity = receiveDependenciesAndSendToMallikas(projectId, completeAddress);
		return entity;
	}
	
	/**
	 * Method that sends requirements and dependencies to UPC services, parses the response and sends received dependencies (new ones have status "proposed" to Mallikas)
	 * @param projectId
	 * @param url
	 * @return
	 * @throws IOException
	 */
	private ResponseEntity<?> receiveDependenciesAndSendToMallikas(String projectId, String url) throws IOException {
		RestTemplate rt = new RestTemplate();
		String response = null;
		ResponseEntity<?> entity = null;
		try {
			String jsonString = getProjectRequirementsFromMallikas(projectId, mallikasAddress + "projectRequirements");
			if(jsonString!=null) {
				HttpHeaders headers = new HttpHeaders();
				headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
				headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON_UTF8));
				
				HttpEntity<String> entity2 = new HttpEntity<String>(jsonString, headers);
				response = rt.postForObject(url, entity2, String.class);
			if(response!=null) {
				JSONParser.parseToOpenReqObjects(response);
				List<Dependency> dependencies = JSONParser.dependencies;
				entity = millaController.postDependenciesToMallikas(dependencies);
			}		
			}

		} catch (HttpClientErrorException e) {
			return new ResponseEntity<>("UPC error:\n\n" + e.getResponseBodyAsString(), e.getStatusCode());
		}
		catch (JSONException e) {
			e.printStackTrace();
			return new ResponseEntity<>("Error in parsing JSON ", HttpStatus.NO_CONTENT);
		}
		return entity;
	}
	
	/**
	 * Fetches requirements in a project from Mallikas
	 * @param projectId
	 * @param url
	 * @return
	 */
	private String getProjectRequirementsFromMallikas(String projectId, String url) {
		try {
			String requirements = mallikasService.getAllRequirementsInProjectFromMallikas(projectId, url);
			return requirements;
		}
		catch (HttpClientErrorException e) {
			e.printStackTrace();
			return null;
		}
	}
	
}
