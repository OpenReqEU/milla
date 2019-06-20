package eu.openreq.milla.controllers;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Queue;

import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

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
	
	@Value("${milla.detectionGetAddresses}")
	private String[] detectionGetAddresses;
	
	@Value("${milla.detectionPostAddresses}")
	private String[] detectionPostAddresses;
	
	@Value("${milla.organization}")
	private String organization;

	@Autowired
	MallikasService mallikasService;
	
	@Autowired
	MillaController millaController;
	
	@Autowired
	RestTemplate rt;
	
	Queue<String> responseIds;

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
	public ResponseEntity<?> postRequirementsToUPCSimilarityDetection(@RequestParam String projectId)
			throws IOException {
		String receiveAddress = millaAddress + "/receiveAddReqResponse";
		return postReqsToUPC(projectId, "AddReqs?url=" + receiveAddress + "&organization=" + organization);
	}
	
	/**
	 * Post requirements to UPC dependency detection and detect similarity between all of them
	 * @param projectId
	 * @param threshold
	 * @return
	 * @throws IOException
	 */
	@ApiOperation(value = "Post requirements to UPC dependency detection and compute all")
	@PostMapping(value = "detectSimilarityAddReqsAndCompute")
	public ResponseEntity<?> postToUPCAddReqsAndCompute(@RequestParam String projectId, 
			@RequestParam(required = false, defaultValue = "0.3") Double threshold) 
			throws IOException {
		String receiveAddress = millaAddress + "/receiveSimilarities";
		ResponseEntity<String> response = postReqsToUPC(projectId, "AddReqsAndCompute?threshold=" + threshold 
				+ "&url=" + receiveAddress + "&organization=" + organization);
		
		try {
			System.out.println(response);
			JsonObject object = new Gson().fromJson(response.getBody(), JsonObject.class);
			
			if (object.has("id")) {
				if (responseIds==null) {
					responseIds = new ArrayDeque<String>();
				}
				String responseId = object.get("id").toString();
				//responseId = responseId.replace("\"", "");
				System.out.println("Added ID to queue: " + responseId);
				responseIds.add(responseId);				
			}
			
		} catch (JSONException e) {
			return response;
		}
		
		
		return response;
	}
	
	/**
	 * Posts the requirements to UPC detection
	 * @param projectId
	 * @param urlTail
	 * @return
	 */
	private ResponseEntity<String> postReqsToUPC(String projectId, String urlTail) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
		headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON_UTF8));

		String requirements = mallikasService.getAllRequirementsInProject(projectId, false);
		String completeAddress = upcSimilarityAddress + "/upc/similarity-detection/" + urlTail;
		
		HttpEntity<String> entity = new HttpEntity<String>(requirements, headers);
		try {
			ResponseEntity<String> response = rt.postForEntity(completeAddress, entity, String.class);
			
			return new ResponseEntity<String>(response.getBody() + "", HttpStatus.ACCEPTED);
			
		} catch (HttpClientErrorException e) {
			return new ResponseEntity<>("UPC error:\n\n" + e.getResponseBodyAsString(), e.getStatusCode());
		}
	}
	
	
//	/**
//	 * Receive the confirmation that adding requirements to similarity detection has begun
//	 * @param result
//	 * @throws IOException
//	 */
//	@ApiIgnore
//	@PostMapping(value = "receiveAddReqResponse")
//	public void receiveAddReqResponse(@RequestParam MultipartFile result)
//			throws IOException{
//		
//		String content = new String(result.getBytes());
//
//		try {
//			JsonObject object = new Gson().fromJson(content, JsonObject.class);
//			System.out.println(object.toString());
//			
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
//		} catch (JSONException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		
//	}
	
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
			@RequestParam String projectId, @RequestParam(required = false, 
			defaultValue = "0.3") Double threshold)
			throws IOException {
		
		String thisAddress = millaAddress + "/receiveSimilarities";
		
		String completeAddress = upcSimilarityAddress
				+ "/upc/similarity-detection/Project?compare=" + compare + "&project=" + projectId  + 
				"&threshold=" + threshold + "&url=" + thisAddress + "&organization=" + organization;
		
		String jsonString = mallikasService.getAllRequirementsInProject(projectId, true);
		
		return sendRequirementsForDetection(jsonString, completeAddress);

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
			@RequestParam String projectId, @RequestParam List<String> requirementId, @RequestParam(required = false, 
			defaultValue = "0.3") Double threshold)
			throws IOException{
		
		String thisAddress = millaAddress + "/receiveSimilarities";
		
		String reqsString = "";
		
		for (String id : requirementId) {
			reqsString = reqsString + "&req=" + id;
		}
		
		String completeAddress = upcSimilarityAddress + "/upc/similarity-detection/ReqProject?compare=" + 
		compare + "&project=" + projectId + reqsString + "&threshold=" + threshold + "&url=" + 
				thisAddress + "&organization=" + organization;
		
		String jsonString = mallikasService.getAllRequirementsInProject(projectId, true);
		
		return sendRequirementsForDetection(jsonString, completeAddress);

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
	public ResponseEntity<?> postRequirementsToUPCSimilarityDetectionReqReq(@RequestParam Boolean compare, 
			@RequestParam String requirementId1, @RequestParam String requirementId2)
			throws IOException{

		String thisAddress = millaAddress + "/receiveSimilarities";
		String completeAddress = upcSimilarityAddress + "/upc/similarity-detection/ReqReq?compare=" + compare + 
				"&req1=" + requirementId1 + "&req2=" + requirementId2 + "&url=" + thisAddress + "&organization=" + organization;
		
		List<String> ids = Arrays.asList(requirementId1, requirementId2);
		
		String jsonString = mallikasService.getSelectedRequirements(ids);
				
		return sendRequirementsForDetection(jsonString, completeAddress);
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
					+ "<b>Postcondition</b>: After successful detection, detected cross references "
					+ "are stored in Mallikas as proposed dependencies."
					+ "<br><b>Parameter: </b>"
					+ "<br>projectId: The project id in Mallikas (e.g., QTWB).")
	@PostMapping(value = "detectCrossReferenceProject")
	public ResponseEntity<?> postRequirementsToUPCCrossReferenceDetectionProject(@RequestParam String projectId)
			throws IOException {

		String completeAddress = upcCrossReferenceAddress
				+ "/upc/cross-reference-detection/json/"+ projectId;

		return sendRequirementsForDetection(projectId, completeAddress);
	}
	
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
	private ResponseEntity<String> sendRequirementsForDetection(String jsonString, String url) {
		
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
			headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON_UTF8));
			HttpEntity<String> entity = new HttpEntity<String>(jsonString, headers);
			
			try {
				ResponseEntity<String> response = rt.postForEntity(url, entity, String.class);	
				if(response==null) {
					return new ResponseEntity<String>("No response", HttpStatus.NOT_FOUND);
				}
				try {
					JsonObject object = new Gson().fromJson(response.getBody(), JsonObject.class);
					if (object.has("id")) {
						String responseId = object.get("id").getAsString();
						//responseId = responseId.replace("\"", "");
						
						System.out.println("Added ID to queue: " + responseId);
						
						if (responseIds==null) {
							responseIds = new ArrayDeque<String>();
						}
						
						responseIds.add(responseId);				
					}
				} catch (JSONException e) {
					return new ResponseEntity<String>("No valid JSON object received", response.getStatusCode());
				}
				return new ResponseEntity<String>(response.getBody() + "", response.getStatusCode());
			} catch (RestClientException e) {
				return new ResponseEntity<String>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
			}

	}
		
	/**
	 * Receives the dependencies from similarity detection
	 * @param result
	 * @throws IOException
	 */
	@ApiIgnore
	@PostMapping(value = "receiveSimilarities")
	public String receiveSimilarities(@RequestParam String result) throws IOException {
		String content = new String(result.getBytes());
		
		try {
			JsonObject object = new Gson().fromJson(content, JsonObject.class);
			
			if (object.has("error")) {
				return object.get("error").toString();
			} else {
				addDependenciesToMallikas(content);
				return object.toString();
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Sends dependencies provided to Mallikas
	 * 
	 * @param content
	 * @return
	 * @throws IOException
	 */
	public ResponseEntity<String> addDependenciesToMallikas(String content)
			throws IOException{	
		String response = null;
		
		try {
			JSONParser.parseToOpenReqObjects(content);
			List<Dependency> dependencies = JSONParser.dependencies;
			if (dependencies==null) {
				return new ResponseEntity<String>("Dependencies null", HttpStatus.BAD_REQUEST);
			}
			response = (String)millaController.postDependenciesToMallikas(dependencies, true).getBody();
		} catch (HttpClientErrorException e) {
			return new ResponseEntity<String>("Error:\n" + e.getResponseBodyAsString(), e.getStatusCode());
		}
		catch (JSONException|com.google.gson.JsonSyntaxException e) {
			return new ResponseEntity<String>("Error in parsing JSON", HttpStatus.NO_CONTENT);
		}

		return new ResponseEntity<String>(response, HttpStatus.OK);
	}
	
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
	@GetMapping(value = "getDetectedFromService")
	private ResponseEntity<String> getDependenciesFromDetectionService(String url, String requirementId) throws IOException {
		
		ResponseEntity<String> serviceResponse = rt.getForEntity(url + requirementId, String.class);
		
		ResponseEntity<String> mallikasResponse = addDependenciesToMallikas(serviceResponse.getBody());
		
		return new ResponseEntity<String>(serviceResponse.getBody(), 
				mallikasResponse.getStatusCode());	
		
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
	@PostMapping(value = "postProjectToService")
	private ResponseEntity<String> postRequirementsToDetectionService(@RequestParam String url, 
			@RequestParam String projectId, @RequestBody(required = false) String jsonString) throws IOException {
		
		if (jsonString==null) {		
			jsonString = mallikasService.getAllRequirementsInProject(projectId, true);
		}
		
		ResponseEntity<String> serviceResponse = (ResponseEntity<String>)sendRequirementsForDetection(jsonString, url);
		
		ResponseEntity<String> mallikasResponse = addDependenciesToMallikas(serviceResponse.getBody());
		
		return new ResponseEntity<String>(mallikasResponse.getBody() + "\n" + serviceResponse.getBody(), 
				mallikasResponse.getStatusCode());	
		
	}
	
	/**
	 * Fetch the results of similarity detection by the ID of the process
	 * @param responseId
	 * @return
	 * @throws IOException
	 */
	@ApiOperation(value = "Fetch the result from UPC detection service")
	@GetMapping(value = "getResponse")
	public ResponseEntity<?> getResponseFromUPC(@RequestParam(required = false) String responseId) {
		
		if (responseIds==null || responseIds.isEmpty()) {
			return new ResponseEntity<String>("No IDs queued", HttpStatus.BAD_REQUEST);	
		}
		if (responseId==null) {
			responseId = responseIds.poll();
			System.out.println("Removed ID from queue: " + responseId);
		} else {
			if (responseIds.contains(responseId)) {
				responseIds.remove(responseId);
				System.out.println("Removed ID from queue: " + responseId);
			} else {
				return new ResponseEntity<String>("No such ID queued", HttpStatus.BAD_REQUEST);	
			}
		}

		String completeAddress = upcSimilarityAddress + "/upc/similarity-detection/GetResponse?"
				+ "organization=" + organization + "&response=" + responseId;

		System.out.println(completeAddress);
		try {
			ResponseEntity<String> response = rt.getForEntity(completeAddress, String.class); 
			System.out.println(response);
			if (response.getStatusCode()!=HttpStatus.OK) {
				responseIds.add(responseId);
				return new ResponseEntity<String>("Dependencies not saved: \n\n" + response.getBody(), response.getStatusCode());
			}
			String result = receiveSimilarities(response.getBody());
			return new ResponseEntity<String>("Dependencies received and saved: \n\n" + result, HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<String>("Couldn't receive dependencies", HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	/**
	 * Request all services to return results for requirement id
	 * @param requirementId
	 * @return
	 * @throws IOException
	 */
	@ApiOperation(value = "Get results from all detection services for the requirement id")
	@PostMapping("getDetectedFromServices")
	public ResponseEntity<String> getDetectedFromServices(@RequestParam String requirementId) throws IOException {
		List<Dependency> dependencies = new ArrayList<>();
		for (String url : detectionGetAddresses) {
			ResponseEntity<String> detectionResult = getDependenciesFromDetectionService(url, requirementId);
			try {
				JSONParser.parseToOpenReqObjects(detectionResult.getBody().toString());
				dependencies.addAll(JSONParser.dependencies);
			} catch (JSONException|com.google.gson.JsonSyntaxException e) {
				System.out.println("Did not receive valid JSON from " + url + " :\n" + detectionResult.getBody());
			}
		}		
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
	@PostMapping("postProjectToServices")
	public ResponseEntity<String> postProjectToServices(@RequestParam String projectId) throws IOException {
		String jsonString = mallikasService.getAllRequirementsInProject(projectId, true);
		String results = "";
		
		for (String url : detectionPostAddresses) {
			ResponseEntity<String> postResult = postRequirementsToDetectionService(url, null, jsonString);
			results += "Response from " + url + " \n" + postResult + "\n\n";
		}
		
		return new ResponseEntity<String>(results, HttpStatus.OK);
		
	}
	
	
}
