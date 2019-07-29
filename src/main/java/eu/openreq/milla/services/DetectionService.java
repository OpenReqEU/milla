package eu.openreq.milla.services;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
	
	@Value("${milla.ownAddress}")
	private String millaAddress;
	
	@Autowired
	MallikasService mallikasService;
	
	@Autowired
	RestTemplate rt;
	
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
	public ResponseEntity<String> sendRequirementsForDetection(String jsonString, String url) {
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
}
