package eu.openreq.milla.services;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
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
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import eu.openreq.milla.models.json.Dependency;


@Service
public class DetectionService {
	
	@Value("${milla.upcSimilarityAddress}")
	private String upcSimilarityAddress;
	
	@Value("${milla.upcCrossReferenceAddress}")
	private String upcCrossReferenceAddress;

	@Value("${milla.detectionGetAddresses}")
	private String[] detectionGetAddresses;
	
	@Value("${milla.detectionGetPostAddresses}")
	private String detectionGetPostAddresses;
	
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
	
	public List<Dependency> getDetectedFromServices(String requirementId) {
		List<Dependency> dependencies = new ArrayList<>();
		dependencies.addAll(getDetectedByGetOrPost(requirementId, Arrays.asList(detectionGetAddresses), false));
		dependencies.addAll(getDetectedByGetOrPost(requirementId, Arrays.asList(detectionGetPostAddresses), true));

		return dependencies;
	}
	
	private List<Dependency> getDetectedByGetOrPost(String requirementId, List<String> urls, boolean byPost) {
		List<Dependency> dependencies = new ArrayList<>();

		for (String url : urls) {
			String responseBody = "No response";
			try {
				ResponseEntity<String> serviceResponse;
				if (byPost) {
					serviceResponse = rt.postForEntity(url + requirementId, null, String.class);
				} else {
					serviceResponse = rt.getForEntity(url + requirementId, String.class);
				}
				
				responseBody = serviceResponse.getBody();
				OpenReqJSONParser parser = new OpenReqJSONParser(responseBody);
				List<Dependency> foundDeps = parser.getDependencies();
				if (foundDeps!=null) {
					dependencies.addAll(foundDeps);
				}	
			} catch (JSONException|com.google.gson.JsonSyntaxException e) {
				System.out.println("Did not receive valid JSON from " + 
						url + " :\n" + responseBody);
			} catch (Exception e) {
				System.out.println("Error connecting to address " + url + " :\n" + e.getMessage());
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
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
		HttpEntity<String> entity = new HttpEntity<String>(jsonString, headers);
		try {
			ResponseEntity<String> response = rt.postForEntity(url, entity, String.class);	
			if(response==null) {
				return new ResponseEntity<String>("No response", HttpStatus.NOT_FOUND);
			}
			return new ResponseEntity<String>(response.getBody() + "", response.getStatusCode());
		} catch (Exception e) {
			return new ResponseEntity<String>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	/**
	 * Post the string as a file to a detection service
	 * @param name
	 * @param file
	 * @param formParams
	 * @param completeAddress
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
		
		String completeAddress = upcSimilarityAddress + "/upc/similarity-detection/BuildClusters";
		
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
		
		JsonElement depsArray = new Gson().toJsonTree(dependencies);	
		JsonObject jsonObject = new JsonObject();
		jsonObject.add("dependencies", depsArray);
		
		HttpEntity<String> entity = new HttpEntity<String>(jsonObject.toString(), headers);

		try {
			ResponseEntity<String> response = rt.postForEntity(completeAddress, entity, String.class);
			return new ResponseEntity<String>(response.getBody(), response.getStatusCode());
		} catch (HttpClientErrorException|HttpServerErrorException e) {
			return new ResponseEntity<>(e.getResponseBodyAsString(), e.getStatusCode());
		} catch (Exception e) {
			return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}	
	
	public ResponseEntity<String> postProjectToService(String projectId, String url, String jsonString) {
		if (jsonString == null) {
			jsonString = mallikasService.getAllRequirementsInProject(projectId, true, false);
		}
		
		ResponseEntity<String> serviceResponse = postStringToService(jsonString, url);;
		
		String mallikasResponse = mallikasService.convertAndUpdateDependencies(serviceResponse.getBody(), true, false).getBody();
		
		return new ResponseEntity<String>("\nService response: " + serviceResponse.getBody() + 
				"\nMallikas response: " + mallikasResponse, serviceResponse.getStatusCode());	
	}
	
	public ResponseEntity<String> postProjectToServices(String projectId) {	
		String jsonString;
		
		if (projectId.equals("ALL")) {
			jsonString = mallikasService.getAllRequirements();
		} else {
			jsonString = mallikasService.getAllRequirementsInProject(projectId, true, false);
		}
		
		String results = "";
		
		for (String url : detectionPostAddresses) {
			ResponseEntity<String> postResult = postProjectToService(null, url, jsonString);
			results += "Response from " + url + " \n" + postResult + "\n\n";
		}
		
		return new ResponseEntity<String>(results, HttpStatus.OK);
	}
	
	public String postUpdatesToServices(String projectId, String content) {
		String response = "Update responses from services:";
		for (String url : detectionUpdateAddresses) {
			ResponseEntity<String> updateResponse = postStringToService(content, url);
			mallikasService.convertAndUpdateDependencies(updateResponse.getBody(), true, false);
			response += "\n" + url + " : " + updateResponse.getBody();
		}
		return response;
	}
	
}
