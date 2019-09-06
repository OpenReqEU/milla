package eu.openreq.milla.services;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;

import eu.openreq.milla.models.jira.Issue;
import eu.openreq.milla.models.json.Dependency;
import eu.openreq.milla.models.json.Person;
import eu.openreq.milla.models.json.Project;
import eu.openreq.milla.models.json.Requirement;
import eu.openreq.milla.qtjiraimporter.ProjectIssues;

@Service
public class ImportService {
	
	@Autowired
	MallikasService mallikasService;
	
	@Autowired
	UpdateService updateService;
	
	@Autowired
	FormatTransformerService transformer;
	
	Gson gson = new Gson();

	public ResponseEntity<String> importProjectIssues(String projectId, OAuthService authService) throws Exception {
		
		ProjectIssues projectIssues = new ProjectIssues(projectId, authService);

		Person person = new Person();
		person.setUsername("user_" + projectId);
		person.setEmail("dummyEmail");

		int issueCount = projectIssues.getNumberOfIssues();
		if(issueCount<=0) {
			return new ResponseEntity<>("No issues found, download failed", HttpStatus.BAD_REQUEST);
		}
		int divided = issueCount;
		if (issueCount > 10000) { // this is necessary for large Qt Jira projects
			divided = issueCount / 10;
		}
		int start = 1;
		int end = divided;

		// int epicCount = 0; //these needed for counting epics and subtask
		// relationships in projects
		// int subtaskCount = 0; //Note! to use these, must uncomment lines in
		// FormatTransformerService

		long start1 = System.nanoTime();
		
		List<String> requirementIds = new ArrayList<>();
		Collection<JsonElement> projectIssuesAsJson;
		
		System.out.println("Importing " + projectId + " from Jira");
		
		try {
			while (true) { // a loop needed for sending large projects in chunks to Mallikas
				if (end >= issueCount + divided) {
					break;
				}
				
				projectIssuesAsJson = projectIssues.collectIssues(start, end);
				List<Issue> issues = transformer.convertJsonElementsToIssues(projectIssuesAsJson);
				Collection<Requirement> requirements = transformer.convertIssuesToJson(issues, projectId, person);
				Collection<Dependency> dependencies = transformer.getDependencies();
				// epicCount = epicCount + transformer.getEpicCount();
				// subtaskCount = subtaskCount + transformer.getSubtaskCount();
				requirementIds.addAll(transformer.getRequirementIds());
				mallikasService.updateRequirements(requirements, projectId);
				mallikasService.updateDependencies(dependencies, false, false);
				projectIssuesAsJson.clear();
				issues.clear();
				requirements.clear();
				dependencies.clear();
				System.out.println("End is " + end);
				start = end + 1;
				end = end + divided;
			}

			Project project = transformer.createProject(projectId, requirementIds);
			mallikasService.postProject(project);

			long end1 = System.nanoTime();
			long durationSec = (end1 - start1) / 1000000000;
			double durationMin = durationSec / 60.0;
			System.out.println("Download done, it took " + durationSec + " second(s) or " + durationMin + " minute(s).");

			return new ResponseEntity<String>("All requirements and dependencies downloaded",
					HttpStatus.OK);
			
		} catch (RestClientException e) {
			System.out.println(e.getMessage());
		}
		return new ResponseEntity<>("Download failed", HttpStatus.BAD_REQUEST);
	}
	
	public ResponseEntity<String> importUpdatedIssues(List<String> projectId, OAuthService authService) {
		try { 
			String projectList = mallikasService.getListOfProjects();
			Type mapType = new TypeToken<HashMap<String, Integer>>(){}.getType();
			HashMap<String, Integer> map = gson.fromJson(projectList, mapType);
			Set<String> ids = map.keySet();
			
			for (String id : projectId) {
				if (ids==null || !ids.contains(id)) {
					return new ResponseEntity<String>("No project " + id + " found, must import first before updating", 
							HttpStatus.NOT_FOUND);
				}
			}	
			return updateService.getAllUpdatedIssues(projectId, authService);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		
		return new ResponseEntity<>("Download failed", HttpStatus.BAD_REQUEST);
	}

}
