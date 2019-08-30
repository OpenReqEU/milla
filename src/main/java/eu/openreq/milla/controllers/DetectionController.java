package eu.openreq.milla.controllers;

import java.io.IOException;
import java.util.List;
import java.util.Queue;

import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import eu.openreq.milla.models.json.Dependency;
import eu.openreq.milla.services.DetectionService;
import eu.openreq.milla.services.MallikasService;
import io.swagger.annotations.ApiOperation;
import springfox.documentation.annotations.ApiIgnore;

@RestController
public class DetectionController {

	@Autowired
	DetectionService detectionService;
	
	@Autowired
	RestTemplate rt;
	
	Queue<String> responseIds;
	
	@Autowired
	MallikasService mallikasService;
	
	
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
//				+ "/upc/cross-reference-detection/json?projectId=" + projectId;
//
//		return sendRequirementsForDetection(projectId, completeAddress);
//	}
	
	/**
	 * Get detected dependencies from some detection service
	 * 
	 * @param requirementId
	 * @param url
	 * @return ResponseEntity<?>
	 * @throws IOException
	 */
	@ApiOperation(value = "Get results from a detection service")
	@ApiIgnore
	@GetMapping(value = "detectedFromService")
	private ResponseEntity<String> getDependenciesFromDetectionService(String requirementId, String url) throws IOException {
		return detectionService.getDetectedFromService(requirementId, url);
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
	@ApiIgnore
	@PostMapping(value = "projectToService")
	private ResponseEntity<String> postRequirementsToDetectionService( 
			@RequestParam String projectId, @RequestParam String url) throws IOException {
		return detectionService.postProjectToService(projectId, url, null);
	}
	
	/**
	 * Request all services to return results for requirement id
	 * @param requirementId
	 * @return
	 * @throws IOException
	 */
	@ApiOperation(value = "Get results from all detection services for the requirement id")
	@GetMapping("detectedFromServices")
	public ResponseEntity<String> getDetectedFromServices(@RequestParam String requirementId) throws IOException {
		List<Dependency> dependencies = detectionService.getDetectedFromServices(requirementId);
		
		JsonObject resultObj = new JsonObject();
		resultObj.add("dependencies", new Gson().toJsonTree(dependencies));
		
		return new ResponseEntity<String>(resultObj.toString(), HttpStatus.OK);
	}
	

	/**
	 * Fetch a project from Mallikas and post it to all services defined in properties
	 * @param projectId
	 * @return
	 * @throws IOException
	 */
	@ApiOperation(value = "Post a project to all detection services")
	@PostMapping("projectToServices")
	public ResponseEntity<String> postProjectToServices(@RequestParam String projectId) throws IOException {
		return detectionService.postProjectToServices(projectId);
	}
	
	
	/**
	 * Fetch a project from Mallikas and post it to ORSI
	 * @param projectId
	 * @return
	 * @throws IOException
	 */
	@ApiOperation(value = "Post a project to ORSI cluster computation")
	@PostMapping("projectToORSI")
	public ResponseEntity<String> postProjectToORSI(@RequestParam String projectId, @RequestParam(required = false, 
			defaultValue = "0.3") double threshold) throws IOException {
		String jsonString;
		
		if (projectId.equals("ALL")) {
			jsonString = mallikasService.getAllRequirements();
		} else {
			jsonString = mallikasService.getAllRequirementsInProject(projectId, true, false);
		}
		
		return detectionService.postFileToOrsi(projectId, jsonString, threshold);
	}
	
	
	/**
	 * Post accepted/rejected dependencies to ORSI
	 * @param projectId
	 * @return
	 * @throws IOException
	 */
	@ApiIgnore
	@ApiOperation(value = "Post accepted and rejected to ORSI cluster computation")
	@PostMapping("acceptedAndRejectedToORSI")
	public ResponseEntity<String> acceptedAndRejectedToORSI(@RequestBody List<Dependency> dependencies) throws IOException {
		return detectionService.acceptedAndRejectedToORSI(dependencies);
	}
	
	/**
	 * Receive the confirmation that adding requirements to similarity detection has begun
	 * @param result
	 * @throws IOException
	 */
	@ApiIgnore
	@PostMapping(value = "receiveAddReqResponse")
	public ResponseEntity<String> receiveAddReqResponse(@RequestParam MultipartFile result)
			throws IOException{
		
		System.out.println("Received response");
		String content = new String(result.getBytes());

		try {
			JsonObject object = new Gson().fromJson(content, JsonObject.class);
			System.out.println(object.toString());
		} catch (JSONException e) {
			System.out.println(e.getMessage());
		}
		
		return new ResponseEntity<String>("Response received", HttpStatus.OK);
	}
	
	
	
	
	
}
