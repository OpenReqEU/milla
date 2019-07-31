package eu.openreq.milla.services;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.google.gson.Gson;

import eu.openreq.milla.models.json.Dependency;


@Service
public class DetectionService {
	
	@Value("${milla.upcSimilarityAddress}")
	private String upcSimilarityAddress;
	
	@Value("${milla.upcCrossReferenceAddress}")
	private String upcCrossReferenceAddress;

	@Value("${milla.detectionGetAddresses}")
	private String[] detectionGetAddresses;
	
	@Value("${milla.detectionPostAddresses}")
	private String[] detectionPostAddresses;
	
	@Value("${milla.detectionUpdateAddresses}")
	private String[] detectionUpdateAddresses;
	
	@Value("${milla.ownAddress}")
	private String millaAddress;
	
	@Autowired
	MallikasService mallikasService;
	
	@Autowired
	RestTemplate rt;
	
	public ResponseEntity<String> getDetectedFromService(String requirementId, String url) {
		try {
			ResponseEntity<String> serviceResponse = rt.getForEntity(url + requirementId, String.class);	
			
			return new ResponseEntity<String>(serviceResponse.getBody(), 
					serviceResponse.getStatusCode());
		} catch (Exception e) {
			return new ResponseEntity<String>(e.getMessage(), HttpStatus.NO_CONTENT);
		}	
	}
	
	public List<Dependency> getDetectedFromServices(String requirementId)
	{
		List<Dependency> dependencies = new ArrayList<>();
		for (String url : detectionGetAddresses) {
			ResponseEntity<String> detectionResult = getDetectedFromService(requirementId, url);
			try {
				OpenReqJSONParser parser = new OpenReqJSONParser(detectionResult.getBody().toString());
				List<Dependency> foundDeps = parser.getDependencies();
				if (foundDeps!=null) {
					dependencies.addAll(foundDeps);
				}
			} catch (JSONException|com.google.gson.JsonSyntaxException e) {
				System.out.println("Did not receive valid JSON from " + url + " :\n" + detectionResult.getBody());
			}
		}		

		return dependencies;
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
	public ResponseEntity<String> postStringToService(String jsonString, String url) {
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
			headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON_UTF8));
			HttpEntity<String> entity = new HttpEntity<String>(jsonString, headers);
			
			try {
				ResponseEntity<String> response = rt.postForEntity(url, entity, String.class);	
				if(response==null) {
					return new ResponseEntity<String>("No response", HttpStatus.NOT_FOUND);
				}
				return new ResponseEntity<String>(response.getBody() + "", response.getStatusCode());
			} catch (RestClientException e) {
				return new ResponseEntity<String>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
			}

	}
	
	/**
	 * Posts the requirements to UPC detection
	 * @param projectId
	 * @param urlTail
	 * @return
	 * @throws UnsupportedEncodingException 
	 */
	public ResponseEntity<String> postFileToService(String name, String file, Map<String, String> formParams, String completeAddress) throws UnsupportedEncodingException {
		
		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON_UTF8));
		headers.setContentType(MediaType.MULTIPART_FORM_DATA);
		
		LinkedMultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
		
		ByteArrayResource contentsAsResource = new ByteArrayResource(file.getBytes("UTF-8")){
            @Override
            public String getFilename(){
                return name;
            }
        };
        
	    params.add("file", contentsAsResource);
	    
	    for (String key : formParams.keySet()) {
	    	params.add(key, formParams.get(key));
	    }
	
	    HttpEntity<LinkedMultiValueMap<String, Object>> entity =
	            new HttpEntity<>(params, headers);
		
		try {
			ResponseEntity<String> response = rt.postForEntity(completeAddress, entity, String.class);
			
			return new ResponseEntity<String>(response.getBody() + "", HttpStatus.ACCEPTED);
			
		} catch (HttpClientErrorException e) {
			return new ResponseEntity<>("Error:\n\n" + e.getResponseBodyAsString(), e.getStatusCode());
		}
	}
	
	public ResponseEntity<String> postFileToOrsi(String projectId, String jsonString, Double threshold) throws UnsupportedEncodingException {
	
		String receiveAddress = millaAddress + "/receiveAddReqResponse";
		
		String completeAddress = upcSimilarityAddress + "/upc/similarity-detection/BuildClustersAndCompute";
		
		Map<String, String> formParams = new HashMap<>();
		
		formParams.put("url", receiveAddress);
		formParams.put("threshold", threshold + "");
		formParams.put("organization", "Qt");
		formParams.put("compare", "true");
	
		ResponseEntity<String> response = postFileToService(projectId, jsonString, formParams, completeAddress);
	
		String results = "Response from " + receiveAddress + " \n" + response.toString() + "\n\n";
		
		
		return new ResponseEntity<String>(results, HttpStatus.OK);
	
	}
	
	
	public ResponseEntity<String> acceptedAndRejectedToORSI(List<Dependency> dependencies) {
		
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
		headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON_UTF8));
		
		String completeAddress = upcSimilarityAddress + "/upc/similarity-detection/TreatAcceptedAndRejectedDependencies?organization=Qt";
		
		String jsonString = new Gson().toJson(dependencies);	
		jsonString = jsonString.replaceAll("[\\[\\]]","");
		
		HttpEntity<String> entity = new HttpEntity<String>(jsonString, headers);

		try {
			ResponseEntity<String> response = rt.postForEntity(completeAddress, entity, String.class);
			
			return new ResponseEntity<String>(response.getBody() + "", response.getStatusCode());
			
		} catch (HttpClientErrorException e) {
			return new ResponseEntity<>("Error:\n\n" + e.getResponseBodyAsString(), e.getStatusCode());
		}
	}	
	
	public ResponseEntity<String> postProjectToService(String projectId, String url, String jsonString) {
		
		if (jsonString == null) {
			jsonString = mallikasService.getAllRequirementsInProject(projectId, true, false);
		}
		
		ResponseEntity<String> serviceResponse = postStringToService(jsonString, url);
		
		String mallikasResponse = mallikasService.convertAndUpdateDependencies(serviceResponse.getBody(), true, false);
		
		return new ResponseEntity<String>(mallikasResponse + "\n" + serviceResponse.getBody(), serviceResponse.getStatusCode());	
	}
	
	public ResponseEntity<String> postProjectToServices(String projectId) {	
		String jsonString;
		
		if (projectId == "ALL") {
			jsonString = mallikasService.getAllRequirements();
		} else {
			jsonString = mallikasService.getAllRequirementsInProject(projectId, true, false);
		}
		
		String results = "";
		
		for (String url : detectionPostAddresses) {
			System.out.println(url);
			ResponseEntity<String> postResult = postProjectToService(null, url, jsonString);
			results += "Response from " + url + " \n" + postResult + "\n\n";
		}
		
		return new ResponseEntity<String>(results, HttpStatus.OK);
	}
	
	public String postUpdatesToService(String content) {
		String response = "";
		for (String url : detectionUpdateAddresses) {
			response += postStringToService(content, url) + "\n";
		}
		return response;
	}
}
