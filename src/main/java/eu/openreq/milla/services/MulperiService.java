package eu.openreq.milla.services;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

@Service
public class MulperiService {

	@Value("${milla.mulperiAddress}")
	private String mulperiAddress;
	
	@Autowired
	MallikasService mallikasService;
	
	@Autowired
	RestTemplate rt;

	public ResponseEntity<String> getTransitiveClosure(List<String> requirementId, Integer layerCount) {

		String completeAddress = mulperiAddress + "/models/findTransitiveClosureOfRequirement";

		if (layerCount != null) {
			completeAddress += "?layerCount=" + layerCount;
		}

		try {
			String response = rt.postForObject(completeAddress, requirementId, String.class);
			return new ResponseEntity<>(response, HttpStatus.OK);
		} catch (HttpClientErrorException | HttpServerErrorException e) {
			return new ResponseEntity<>(e.getResponseBodyAsString(), e.getStatusCode());
		}
	}

	/**
	 * Post Requirements and Dependencies to Mulperi.
	 * 
	 * @param data
	 * @param urlTail
	 * @return
	 * @throws IOException
	 */
	private ResponseEntity<String> postToMulperi(Object data, String urlTail) throws IOException {
		
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		String completeAddress = mulperiAddress + urlTail;
		HttpEntity<Object> entity = new HttpEntity<Object>(data, headers);
		try {
			return rt.postForEntity(completeAddress, entity, String.class);
		} catch (HttpClientErrorException e) {
			return new ResponseEntity<>(e.getResponseBodyAsString(), e.getStatusCode());
		}

	}
	
	public ResponseEntity<String> sendProjectToMulperi(String projectId) throws IOException {

		String reqsInProject = mallikasService.getAllRequirementsInProject(projectId, false, false);
		
		if (reqsInProject == null) {
			return new ResponseEntity<>("Requirements not found", HttpStatus.NOT_FOUND);
		}
		return postToMulperi(reqsInProject, "/models/murmeliModelToKeljuCaas");
	}
	
	public ResponseEntity<String> sendProjectUpdatesToMulperi(String updates) throws IOException {
		return postToMulperi(updates, "/models/updateMurmeliModelInKeljuCaas");
	}
	
}
