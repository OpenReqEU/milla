package eu.openreq.milla.controllers;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

//import io.swagger.annotations.*;
import eu.openreq.milla.models.json.Dependency;
import eu.openreq.milla.services.JSONParser;
import eu.openreq.milla.services.MallikasService;
import io.swagger.annotations.ApiOperation;
import springfox.documentation.annotations.ApiIgnore;

@SpringBootApplication
@RestController
public class DetectionController {

	@Value("${milla.ownAddress}")
	private String millaAddress;

	@Value("${milla.upcSimilarityAddress}")
	private String upcSimilarityAddress;
	
	@Value("${milla.upcCrossReferenceAddress}")
	private String upcCrossReferenceAddress;
	
//	private List<String> requestIds;

	@Autowired
	MallikasService mallikasService;
	
	@Autowired
	MillaController millaController;
	
	@Autowired
	RestTemplate rt;
	

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
					+ "<br><b>Parameter: </b>"
					+ "<br>projectId: The project id in Mallikas (e.g., QTWB).")
	@PostMapping(value = "detectSimilarityAddReqs")
	public ResponseEntity<?> postRequirementsToUPCSimilarityDetection(@RequestBody String projectId)
			throws IOException {

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
		headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON_UTF8));

		String requirements = mallikasService.getAllRequirementsInProject(projectId, false);
		String receiveAddress = millaAddress + "/receiveAddReqResponse";
		String completeAddress = upcSimilarityAddress + "/upc/similarity-detection/DB/AddReqs?url=" + receiveAddress;
		
		HttpEntity<String> entity = new HttpEntity<String>(requirements, headers);
		try {
			ResponseEntity<?> response = rt.postForEntity(completeAddress, entity, String.class);
//			if (requestIds==null) {
//				requestIds = new ArrayList<String>();
//			}
//			requestIds.add(response.getBody().toString());
			System.out.println(response.getBody());
			
			return new ResponseEntity<String>(response.getBody() + "", HttpStatus.ACCEPTED);
			
		} catch (HttpClientErrorException e) {
			return new ResponseEntity<>("UPC error:\n\n" + e.getResponseBodyAsString(), e.getStatusCode());
		}
	}
	
	/**
	 * Receive the confirmation that adding requirements to similarity detection has begun
	 * @param result
	 * @throws IOException
	 */
	@ApiIgnore
	@PostMapping(value = "receiveAddReqResponse")
	public void receiveAddReqResponse(@RequestParam MultipartFile result)
			throws IOException{
		
		String content = new String(result.getBytes());

		JSONObject responseObj = null;
		try {
			responseObj = new JSONObject(content);
			System.out.println(responseObj);
			
//			if (!responseObj.isNull("error")) {
//				System.out.println(responseObj.getString("error"));
//			} else {
//				String key = responseObj.getString("id");
//				
//				if (requestIds.contains(key)) {
//					requestIds.remove(key);
//					System.out.println("Request received for key: " + key);
//				}
//			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	/**
	 * Post a Collection of OpenReq JSON Requirements and Dependencies in a project 
	 * to UPC for Similarity detection.
	 * @param compare
	 * @param projectId
	 * @param threshold
	 * @return
	 * @throws IOException
	 */
	@ApiOperation(value = "Detect similarity between all requirements of a project using UPC Similarity Detection", 
			notes = "<b>Functionality</b>: All requirements of a given project are posted to UPC Similarity Detection "
					+ "in order to detect similarity between all requirements. "
					+ "<br><b>Precondition</b>: The project has been cached in Mallikas."
					+ "<br><b>Postcondition</b>: After successful detection, the detected new similarities are "
					+ "stored in Mallikas using the similarity dependency type and proposed status."
					+ "<br><b>Parameters:</b>"
					+ "<br>compare: Whether text attribute is used in comparison"
					+ "<br>projectId: The project id in Mallikas."
					+ "<br>threshold: The minimum score for similarity detection (e.g. 0.3).")
	@PostMapping(value = "detectSimilarityProject")
	public ResponseEntity<?> postRequirementsToUPCSimilarityDetectionProject(@RequestParam Boolean compare, 
			@RequestParam String projectId, @RequestParam String threshold)
			throws IOException {
		
		String thisAddress = millaAddress + "/receiveSimilarities";
		
		String completeAddress = upcSimilarityAddress
				+ "/upc/similarity-detection/Project?compare=" + compare + "&project=" + projectId  + 
				"&threshold=" + threshold + "&url=" + thisAddress;
		
		ResponseEntity<?> entity = sendRequirementsForDetection(projectId, null, completeAddress);
		
		return entity;
	}	
	
	/**
	 * 
	 * @param compare
	 * @param projectId
	 * @param reqId
	 * @param threshold
	 * @return
	 * @throws IOException
	 */
	@ApiOperation(value = "Detect similarity of one requirement against all other requirements of a project using UPC Similarity Detection",  
			notes = "<b>Functionality</b>: All requirements of a given project are posted to UPC Similarity Detection in order to detect similarity between one specified requirements in to project to all other requirements. "
			+ "<br><b>Precondition</b>: The project has been cached in Mallikas."
			+ "<br><b>Postcondition</b>: After successful detection, the detected new similarities are stored"
			+ " in Mallikas using the similarity dependency type and proposed status."
			+ "<br><b>Parameters:</b>"
			+ "<br>compare: Whether the text attribute is used in comparison"
			+ "<br>projectId: The project id in Mallikas."
			+ "<br>requirementId: The ids of the requirements that are to be compared to other requirements in the project."
			+ "<br>threshold: The minimum score for similarity detection (e.g. 0.3).")
	@PostMapping(value = "detectSimilarityReqProject")
	public ResponseEntity<?> postRequirementsToUPCSimilarityDetectionReqProject(@RequestParam Boolean compare, 
			@RequestParam String projectId, @RequestParam List<String> requirementId, @RequestParam String threshold)
			throws IOException{
		
		String thisAddress = millaAddress + "/receiveSimilarities";
		
		String reqsString = "";
		
		for (String id : requirementId) {
			reqsString = reqsString + "&req=" + id;
		}
		
		String completeAddress = upcSimilarityAddress + "/upc/similarity-detection/ReqProject?compare=" + 
		compare + "&project=" + projectId + reqsString + "&threshold=" + threshold + "&url=" + thisAddress;
		
		return sendRequirementsForDetection(projectId, null, completeAddress);
	}	
	
	/**
	 * Post a Collection of OpenReq JSON Requirements and Dependencies to UPC for
	 * comparing two requirements.
	 * @param reqId1
	 * @param reqId2
	 * @return
	 * @throws IOException
	 */
	@ApiOperation(value = "Detect similarity between two requirements using UPC Similarity Detection",  
	notes = "<b>Functionality</b>: Two given requirements are posted to UPC Similarity Detection in order to detect similarity between them."
	+ "<br><b>Precondition</b>: The two requirements have been cached in Mallikas."
	+ "<br><b>Postcondition</b>: After successful detection, the similarity, if above treshold, is stored in Mallikas using the similarity dependency type and proposed status."
	+ "<br><b>Parameters:</b>"
	+ "<br>reqId1: The id of the requirement that is compared to other requirement (reqId2)."
	+ "<br>reqId2: The id of the requirement that is compared to other requirement (reqId1).")
	@PostMapping(value = "detectSimilarityReqReq")
	public ResponseEntity<?> postRequirementsToUPCSimilarityDetectionReqReq(@RequestParam Boolean compare, @RequestParam String requirementId1, 
			@RequestParam String requirementId2)
			throws IOException{

		String thisAddress = millaAddress + "/receiveSimilarities";
		String completeAddress = upcSimilarityAddress + "/upc/similarity-detection/ReqReq?compare=" + compare + 
				"&req1=" + requirementId1 + "&req2=" + requirementId2 + "&url=" + thisAddress;
		
		List<String> ids = Arrays.asList(requirementId1, requirementId2);
				
		return sendRequirementsForDetection(null, ids, completeAddress);
	}
	
	
//	/**
//	 * Post a Collection of OpenReq JSON Requirements and Dependencies in a project to UPC for Cross-Reference
//	 * detection.
//	 * 
//	 * @param projectId
//	 * @return ResponseEntity<?>
//	 * @throws IOException
//	 */
//	@ApiOperation(value = "Detect cross-references in all requirements of a project using UPC Cross-Reference Detection", 
//			notes = "<b>Functionality</b>: Post all requirements and dependencies in a project as a String to UPC for Cross-Reference Detection. Requires projectId <br>"
//					+ "<b>Precondition</b>: The project has been cached in Mallikas.<br>"
//					+ "<b>Postcondition</b>: After successful detection, detected cross references "
//					+ "are stored in Mallikas as proposed dependencies."
//					+ "<br><b>Parameter: </b>"
//					+ "<br>projectId: The project id in Mallikas (e.g., QTWB).")
//	@PostMapping(value = "detectCrossReferenceProject")
//	public ResponseEntity<?> postRequirementsToUPCCrossReferenceDetectionProject(@RequestParam String projectId)
//			throws IOException {
//
//		String completeAddress = upcCrossReferenceAddress
//				+ "/upc/cross-reference-detection/json/"+ projectId;
//
//		return sendRequirementsForSimilarityDetection(projectId, null, completeAddress);
//	}
	
	/**
	 * Retrieve the requirements from Mallikas based either on the given requirement IDs or project ID,
	 * then send them to the similarity detection server. A URL has to be provided for 
	 * the server to send the dependencies calculated. (Here the response only tells whether the sending was successful)
	 * @param projectId
	 * @param ids
	 * @param url
	 * @return Response from the server, which contains the id of the request if successful. 
	 * @throws IOException
	 */
	private ResponseEntity<String> sendRequirementsForDetection(String projectId, Collection<String> ids, String url) throws IOException {
		String jsonString = "";
		try {
			if (ids!=null) {
				jsonString = mallikasService.getSelectedRequirements(ids);
			} else {
				jsonString = mallikasService.getAllRequirementsInProject(projectId, false);
			}
			if(jsonString!=null) {
				HttpHeaders headers = new HttpHeaders();
				headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
				headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON_UTF8));
				
				HttpEntity<String> entity2 = new HttpEntity<String>(jsonString, headers);
				ResponseEntity<?> response = rt.postForEntity(url, entity2, String.class);
				if(response!=null) {
//					if (requestIds==null) {
//						requestIds = new ArrayList<String>();
//					}
//					requestIds.add(response.getBody().toString());
					System.out.println(response.getBody());
					
					return new ResponseEntity<String>(response.getBody() + "", HttpStatus.ACCEPTED);
				}		
			}

		} catch (HttpClientErrorException e) {
			return new ResponseEntity<>("UPC error:\n\n" + e.getResponseBodyAsString(), e.getStatusCode());
		}
//		catch (JSONException e) {
//			e.printStackTrace();
//			return new ResponseEntity<>("Error in parsing JSON ", HttpStatus.NO_CONTENT);
//		}
		return new ResponseEntity<String>("No requirements to send for detection!", HttpStatus.INTERNAL_SERVER_ERROR);
	}
		
	/**
	 * Receives the dependencies from similarity detection
	 * @param result
	 * @throws IOException
	 */
	@ApiIgnore
	@PostMapping(value = "receiveSimilarities")
	public void receiveSimilarities(@RequestParam MultipartFile result) throws IOException {
		String content = new String(result.getBytes());
		
		try {
			JSONObject responseObj = new JSONObject(content);
			System.out.println(responseObj.toString());
			
			if (!responseObj.isNull("error")) {
				System.out.println(responseObj.getString("error"));
			} else {
//				String key = responseObj.getString("id");
//				
//				if (requestIds.contains(key)) {
//					requestIds.remove(key);
//					System.out.println("Matching request key: " + key);
//					
				addDependenciesToMallikas(content);
//				} else {
//					System.out.println("Unknown request key: " + key);
//				}
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public ResponseEntity<String> addDependenciesToMallikas(String content)
			throws IOException{	
		String response = null;
		
		try {
			JSONParser.parseToOpenReqObjects(content);
			List<Dependency> dependencies = JSONParser.dependencies;
			response = (String)millaController.postDependenciesToMallikas(dependencies, true).getBody();
		} catch (HttpClientErrorException e) {
			return new ResponseEntity<String>("UPC error:\n\n" + e.getResponseBodyAsString(), e.getStatusCode());
		}
		catch (JSONException e) {
			e.printStackTrace();
			return new ResponseEntity<String>("Error in parsing JSON", HttpStatus.NO_CONTENT);
		}

		return new ResponseEntity<String>(response, HttpStatus.ACCEPTED);
	}
	

	/**
	 * Post project requirements and the url of the service, received dependencies are saved to Mallikas
	 * 
	 * @param projectId
	 * @param url
	 * @return ResponseEntity<?>
	 * @throws IOException
	 */
	@ApiOperation(value = "Post requirements to some detection service", 
			notes = "<b>Functionality</b>: Post all requirements and dependencies to some detection service. <br>"
					+ "<b>Precondition</b>: The project has been cached in Mallikas.<br>"
					+ "<b>Postcondition</b>: Successfully detected dependencies are saved in Mallikas.<br>"
					+ "<br>projectId: The project id in Mallikas (e.g., QTWB)."
					+ "<br>url: The url of the service to be used.")
	@PostMapping(value = "otherDetectionService")
	public ResponseEntity<?> postRequirementsToDetectionService(@RequestParam String projectId, @RequestParam String url)
			throws IOException {
		ResponseEntity<String> response = (ResponseEntity<String>)sendRequirementsForDetection(projectId, null, url);
		
		String content = response.getBody();
		
		if (response.getStatusCode() != HttpStatus.ACCEPTED) {
			return new ResponseEntity<String>(content, HttpStatus.BAD_REQUEST);
		}
		
		content = "{\"dependencies\":" + content + "}";
		
		return addDependenciesToMallikas(content);		
		
	}
	
}
