package eu.openreq.milla.services;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import eu.openreq.milla.models.jira.Issue;
import eu.openreq.milla.models.json.Dependency;
import eu.openreq.milla.models.json.Person;
import eu.openreq.milla.models.json.Project;
import eu.openreq.milla.models.json.Requirement;
import eu.openreq.milla.qtjiraimporter.UpdatedIssues;

@Service
public class UpdateService {
	
	@Value("${milla.jiraAddress}")
	private String jiraAddress;

	private UpdatedIssues updatedIssues;

	@Autowired
	private MallikasService mallikasService;
	
	@Autowired
	private DetectionService detectionService;
	
	private List<String> reqIds;
	
	private Collection<Requirement> requirements;
	
	private Collection<Dependency> dependencies;
	
	/**
	 * Downloads at least 100 (latest) updated issues from Qt Jira and sends them as OpenReq JSON Requirements to Mallikas
	 * @param projectId
	 * @return 
	 * @throws Exception
	 */
	public ResponseEntity<String> getAllUpdatedIssues(String projectId, OAuthService authService) throws Exception {		
		Person person = new Person();
		person.setUsername("user_" + projectId);
		person.setEmail("dummyEmail");
		
		try {
			updatedIssues = new UpdatedIssues(projectId, authService);
			int amount = getNumberOfUpdatedIssues(projectId, person);
			System.out.println(amount);
			Set<Requirement> totalRequirements = new HashSet<Requirement>();
			Set<Dependency> totalDependencies = new HashSet<Dependency>();
			Set<String> totalReqIds = new HashSet<String>();
			
			for (int current = 0; current<=amount; current = current + 1000) {
				updatedIssues.collectAllUpdatedIssues(projectId, current);
				processJsonElementsToRequirements(updatedIssues.getProjectIssues(), projectId, person);
				if (requirements!=null && !requirements.isEmpty()) {
					mallikasService.updateRequirements(requirements, projectId);
					totalRequirements.addAll(requirements);
				}
				if (dependencies!=null && !dependencies.isEmpty()) {
					mallikasService.updateDependencies(dependencies, false, false);
					totalDependencies.addAll(dependencies);
				}
				if (reqIds!=null && !reqIds.isEmpty()) {
					mallikasService.updateReqIds(reqIds, projectId);
					totalReqIds.addAll(reqIds);
				}
				updatedIssues.clearIssues();
			}
			
			Project project = new Project();
			
			project.setId(projectId);
			project.setSpecifiedRequirements(new ArrayList<String>(totalReqIds));
			
			Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();	
			
			JsonObject object = new JsonObject();
			object.add("projects", gson.toJsonTree(Arrays.asList(project)));
			object.add("requirements", gson.toJsonTree(totalRequirements));
			object.add("dependencies", gson.toJsonTree(totalDependencies));
			
			System.out.println(detectionService.postUpdatesToService(object.getAsString()));
			
			return new ResponseEntity<>(object.toString(), HttpStatus.OK);
		} catch (HttpClientErrorException e) {
			return new ResponseEntity<>("Mallikas error:\n\n" + e.getResponseBodyAsString(), e.getStatusCode());
		}
	}

	/**
	 * Returns an integer that tells how many of the Qt Jira issues have been updated
	 * @param projectId
	 * @param person
	 * @return
	 * @throws Exception
	 */
	public int getNumberOfUpdatedIssues(String projectId, Person person) throws Exception {
		int start = 0;
		int number = -1;
		int sum = 0;
		while (number != 0) {
			JsonElement element = updatedIssues.getLatestUpdatedIssue(start);
			if (element==null) {
				break;
			}
			List<Requirement> reqs = new ArrayList<Requirement>(processJsonElementsToRequirements(Arrays.asList(element), projectId, person));
			if (reqs.isEmpty()) { 
				break;
			}
			number = compareUpdatedIssueWithTheIssueInMallikas(reqs.get(0));
			sum++;
			//System.out.println("Sum (how many times 100 updated issues must be fetched): " + sum);
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
		String requirementString = mallikasService.getSelectedRequirements(Arrays.asList(id));
		if (requirementString != null) {
			try {
				OpenReqJSONParser parser = new OpenReqJSONParser(requirementString);
				List<Requirement> receivedReqs = parser.getRequirements();
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
	 * @param person
	 * @return
	 * @throws Exception
	 */
	private Collection<Requirement> processJsonElementsToRequirements(Collection<JsonElement> elements,
			String projectId, Person person) throws Exception {
		FormatTransformerService transformer = new FormatTransformerService();
		if (!elements.isEmpty()) {
			List<Issue> issues = transformer.convertJsonElementsToIssues(elements);
			requirements = transformer.convertIssuesToJson(issues, projectId, person);
			reqIds = transformer.getRequirementIds();
			dependencies = transformer.getDependencies();
		}
		return requirements;
	}
	

}
