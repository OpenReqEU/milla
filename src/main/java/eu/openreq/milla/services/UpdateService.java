package eu.openreq.milla.services;

import java.io.IOException;

import java.util.*;

import org.json.JSONException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.google.gson.JsonElement;

import eu.openreq.milla.models.jira.Issue;
import eu.openreq.milla.models.json.Dependency;
import eu.openreq.milla.models.json.Person;
import eu.openreq.milla.models.json.Requirement;
import eu.openreq.milla.qtjiraimporter.UpdatedIssues;

@Service
public class UpdateService {

	@Value("${milla.mallikasAddress}")
	private String mallikasAddress;

	private UpdatedIssues updatedIssues;

	private FormatTransformerService transformer;

	private MallikasService mallikasService;

	private int start = 100;
	
	private List<String> reqIds;
	
	private Collection<Dependency> dependencies;

	public UpdateService() {
		transformer = new FormatTransformerService();
		mallikasService = new MallikasService();
	}

	/**
	 * Downloads at least 100 (latest) updated issues from Qt Jira and sends them as OpenReq JSON Requirements to Mallikas
	 * @param projectId
	 * @return
	 * @throws Exception
	 */
	public ResponseEntity<?> getAllUpdatedIssues(String projectId, Person person) throws Exception {
		updatedIssues = new UpdatedIssues(projectId);
		transformer.readFixVersionsToHashMap(projectId);
		ResponseEntity<?> response = null;
		try {
			int amount = getNumberOfUpdatedIssues(projectId, person);
			updatedIssues.collectAllUpdatedIssues(projectId, amount);
			Collection<Requirement> requirements = processJsonElementsToRequirements(updatedIssues.getProjectIssues(), projectId, person);
			this.postRequirementsToMallikas(requirements);
			this.postDependenciesToMallikas(dependencies);
			this.postReqIdsToMallikas(reqIds, projectId);
			
			response = new ResponseEntity<>("All updated requirements and dependencies downloaded", HttpStatus.OK);
		} catch (HttpClientErrorException e) {
			return new ResponseEntity<>("Mallikas error:\n\n" + e.getResponseBodyAsString(), e.getStatusCode());
		}
		return response;
	}

	/**
	 * Returns an integer that tells how many of the Qt Jira issues have been updated 
	 * @param projectId
	 * @return
	 * @throws Exception
	 */
	public int getNumberOfUpdatedIssues(String projectId, Person person) throws Exception {
		int number = -1;
		int sum = 0;
		while (number != 0) {
			JsonElement element = updatedIssues.getTheLatestUpdatedIssue(start);
			List<JsonElement> elements = new ArrayList<>();
			elements.add(element);
			List<Requirement> reqs = new ArrayList<Requirement>(processJsonElementsToRequirements(elements, projectId, person));
			number = compareUpdatedIssueWithTheIssueInMallikas(reqs.get(0));
			sum++;
			System.out.println("Sum (how many times 100 updated issues must be fetched): " + sum);
			start = start + 100;
		}

		return sum*100;
	}

	/**
	 * Compares modified_at fields of two requirements, the old version is fetched from the database
	 * @param requirement
	 * @return
	 * @throws JSONException
	 */
	private int compareUpdatedIssueWithTheIssueInMallikas(Requirement requirement) throws JSONException {
		Requirement oldReq = getOldRequirementFromMallikas(requirement.getId());
		if (oldReq != null) {
			if (requirement.getModified_at() > oldReq.getModified_at()) {
				return 1;
			} else {
				return 0;
			}
		}
		return -1;
	}

	/**
	 * Fetches the old version of a selected requirement from the database
	 * @param id
	 * @return
	 */
	private Requirement getOldRequirementFromMallikas(String id) {
		String url = mallikasAddress + "/one";
		String requirementString = mallikasService.getOneRequirementFromMallikas(url, id);
		if (requirementString != null) {
			try {
				JSONParser.parseToOpenReqObjects(requirementString);
				List<Requirement> receivedReqs = JSONParser.requirements;
				Requirement oldReq = receivedReqs.get(0);
				return oldReq;
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	/**
	 * Uses FormatTransformerService to convert JsonElements to OpenReq JSON Requirements
	 * @param elements
	 * @param projectId
	 * @return
	 * @throws Exception
	 */
	private Collection<Requirement> processJsonElementsToRequirements(Collection<JsonElement> elements,
			String projectId, Person person) throws Exception {
		List<Issue> issues = transformer.convertJsonElementsToIssues(elements);
		Collection<Requirement> requirements = transformer.convertIssuesToJson(issues, projectId, person);
		reqIds = transformer.getRequirementIds();
		dependencies = transformer.getDependencies();
		return requirements;
	}
	
	private ResponseEntity<?> postRequirementsToMallikas(Collection<Requirement> requirements) {
		RestTemplate rt = new RestTemplate();
		ResponseEntity<?> response = null;
		try {	
			response = rt.postForEntity(mallikasAddress + "/updateRequirements", requirements, Collection.class);
		} catch (HttpClientErrorException e) {
			return new ResponseEntity<>("Mallikas error:\n\n" + e.getResponseBodyAsString(), e.getStatusCode());
		}
		return response;
		
	}
	
	private ResponseEntity<?> postDependenciesToMallikas(Collection<Dependency> dependencies) {
		RestTemplate rt = new RestTemplate();
		ResponseEntity<?> response = null;
		try {	
			response = rt.postForEntity(mallikasAddress + "/updateDependencies", dependencies, Collection.class);
		} catch (HttpClientErrorException e) {
			return new ResponseEntity<>("Mallikas error:\n\n" + e.getResponseBodyAsString(), e.getStatusCode());
		}
		return response;
		
	}
	
	/**
	 * Post the ids of the updated requirements to Mallikas so that the Project object can be updated. 
	 * @param reqIds
	 * @param projectId
	 * @return
	 */
	private ResponseEntity<?> postReqIdsToMallikas(Collection<String> reqIds, String projectId) {
		RestTemplate rt = new RestTemplate();
		ResponseEntity<?> response = null;
		Map<String, Collection> updatedReqs = new HashMap<String, Collection>();
		updatedReqs.put(projectId, reqIds);
		
		try {	
			response = rt.postForEntity(mallikasAddress + "/updateProjectSpecifiedRequirements/"+projectId, updatedReqs, Map.class);
		} catch (HttpClientErrorException e) {
			return new ResponseEntity<>("Mallikas error:\n\n" + e.getResponseBodyAsString(), e.getStatusCode());
		}
		return response;
		
	}
	

}
