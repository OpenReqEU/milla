package eu.openreq.milla.services;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.NestedServletException;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import eu.openreq.milla.models.TotalDependencyScore;
import eu.openreq.milla.models.jira.Project;
import eu.openreq.milla.models.json.Dependency;
import eu.openreq.milla.models.json.Dependency_status;
import eu.openreq.milla.models.json.RequestParams;

@Service
public class QtService {
	
	@Value("${milla.mulperiAddress}")
	private String mulperiAddress;
	
	@Autowired
	DetectionService detectionService;

	@Autowired
	MallikasService mallikasService;
	
	@Autowired
	ImportService importService;
	
	@Autowired
	OAuthService authService;
	
	@Autowired
	MulperiService mulperiService;
	
	@Autowired
	RestTemplate rt;
	
	private Gson gson = new Gson();
	
	public ResponseEntity<String> getTransitiveClosureOfRequirement(List<String> requirementId, 
			Integer layerCount) throws IOException {

		String completeAddress = mulperiAddress + "/models/findTransitiveClosureOfRequirement";
		
		if (layerCount!=null) {
			completeAddress += "?layerCount=" + layerCount;
		}
				
		try {
			String response = rt.postForObject(completeAddress, requirementId, String.class);		
			return new ResponseEntity<>(response, HttpStatus.OK);
		} catch (HttpClientErrorException|HttpServerErrorException e) {
			return new ResponseEntity<>(e.getResponseBodyAsString(), e.getStatusCode());
		}
	}
	
	public ResponseEntity<String> getDependenciesOfRequirement(String requirementId, 
			 Double scoreThreshold, Integer maxResults) throws IOException {
		
		RequestParams params = new RequestParams();
		List<String> reqIds = new ArrayList<String>();
		reqIds.add(requirementId);
		params.setRequirementIds(reqIds);
		params.setScoreThreshold(scoreThreshold);
		params.setMaxDependencies(maxResults);

		String reqsWithDependencyType = mallikasService.requestWithParams(params, "dependencies");

		if (reqsWithDependencyType == null || reqsWithDependencyType.equals("")) {
			return new ResponseEntity<>("Search failed, requirements not found", HttpStatus.NOT_FOUND);
		}
		return new ResponseEntity<>(reqsWithDependencyType, HttpStatus.OK);
	}
	
	public ResponseEntity<String> getConsistencyCheckForRequirement(List<String> requirementId,
			Integer layerCount, boolean analysisOnly, Integer timeOut, boolean omitCrossProject, boolean omitReqRelDiag) throws IOException {

		String completeAddress = mulperiAddress + "/models/consistencyCheckForTransitiveClosure?analysisOnly=" + analysisOnly + 
				"&timeOut=" + timeOut + "&omitCrossProject=" + omitCrossProject
				+ "&omitReqRelDiag=" + omitReqRelDiag;
		
		if (layerCount!=null) {
			completeAddress += "&layerCount=" + layerCount;
		}
		
		try {
			String response = rt.postForObject(completeAddress, requirementId, String.class);
			return new ResponseEntity<>(response, HttpStatus.OK);
		} catch (HttpClientErrorException|HttpServerErrorException e) {
			return new ResponseEntity<>("Error:\n" + e.getResponseBodyAsString(), e.getStatusCode());
		}
	}
	
	public ResponseEntity<String> getProposedDependenciesOfRequirement(List<String> requirementId, 
			Integer maxResults) throws IOException {
		
		RequestParams params = new RequestParams();
		params.setRequirementIds(requirementId);
		params.setProposedOnly(true);
		if (maxResults!=null) {
			params.setMaxDependencies(maxResults);
		}
		
		String reqWithTopProposed = mallikasService.requestWithParams(params,
				"dependencies");

		if (reqWithTopProposed == null || reqWithTopProposed.equals("")) {
			return new ResponseEntity<>("Search failed, requirements not found", HttpStatus.NOT_FOUND);
		}
		return new ResponseEntity<>(reqWithTopProposed, HttpStatus.OK);
	}
	
	/**
	 * Bloated method where the proposed are fetched from services and Mallikas and their scores are summed up
	 * @param requirementIds
	 * @param maxResults
	 * @return
	 * @throws IOException
	 */
	public ResponseEntity<String> sumScoresAndGetTopProposed(List<String> requirementIds,
			Integer maxResults, String additionalParams) throws IOException {

		RequestParams params = new RequestParams();
		params.setRequirementIds(requirementIds);
		params.setProposedOnly(false);
		params.setIncludeRejected(true);
		
		List<Dependency> proposed = new ArrayList<Dependency>();		
		List<String> acceptedAndRejectedIds = new ArrayList<String>();
		
		//Get from Mallikas
		
		String detectedFromMallikasString = mallikasService.requestWithParams(params,
				"dependencies");
		
		if (detectedFromMallikasString==null) {
			return new ResponseEntity<String>("Search failed, no requirements found", HttpStatus.NOT_FOUND);
		}
		
		OpenReqJSONParser parser = null;
		try {
			parser = new OpenReqJSONParser(detectedFromMallikasString);
			List<Dependency> detectedFromMallikas = parser.getDependencies();
			for (Dependency dep : detectedFromMallikas) {
				if (dep.getStatus()==Dependency_status.PROPOSED) {
					proposed.add(dep);
				} else{
					acceptedAndRejectedIds.add(dep.getFromid() + "_" + dep.getToid());
					acceptedAndRejectedIds.add(dep.getToid() + "_" + dep.getFromid());
				}
			}
	        
		} catch (com.google.gson.JsonSyntaxException e) {
			System.out.println("Error when parsing dependencies saved in Mallikas " + e.getMessage());
		}
		
		//Get from services
		
		for (String reqId : params.getRequirementIds()) {
			List<Dependency> detectedFromServices = detectionService.getDetectedFromServices(reqId, additionalParams);
			for (Dependency dep : detectedFromServices) {
				String id = dep.getFromid() + "_" + dep.getToid();
				if (!acceptedAndRejectedIds.contains(id) && 
						dep.getStatus()==Dependency_status.PROPOSED)
					proposed.add(dep);	
			}
		}
		
		JsonObject results = new JsonObject();
		
		if (proposed.isEmpty()) {
			String reqs = mallikasService.getSelectedRequirements(requirementIds);
			parser = new OpenReqJSONParser(reqs);
			
			results.add("dependencies", new JsonArray());
			results.add("requirements", gson.toJsonTree(parser.getRequirements()));
			return new ResponseEntity<>(results.toString(), HttpStatus.OK);
		}
		
		//Sum the scores (& descriptions)
		
		Map<String, Dependency> detectedWithTotalScore = new HashMap<>();
		List<TotalDependencyScore> topScores = new ArrayList<>();
		
		for (Dependency dep : proposed) {
			String depId = dep.getFromid() + "_" + dep.getToid();
			String reverseId = dep.getToid() + "_" + dep.getFromid();
			
			if (detectedWithTotalScore.containsKey(reverseId)) {
				dep.setId(reverseId);
			} else {
				dep.setId(depId);
			}
			
			if (detectedWithTotalScore.containsKey(dep.getId())) {
				Dependency totalDep = detectedWithTotalScore.get(dep.getId());
				totalDep.setDependency_score(totalDep.getDependency_score() + dep.getDependency_score());	
				if (totalDep.getDescription()!=null) {
					Set<String> desc = new HashSet<>(totalDep.getDescription());
					desc.addAll(dep.getDescription());
					totalDep.setDescription(new ArrayList<>(desc));
				}
				dep = totalDep;	
			} 
			detectedWithTotalScore.put(dep.getId(), dep);			
		}
		
		for (String key : detectedWithTotalScore.keySet()) { 
			Dependency dep = detectedWithTotalScore.get(key);
			topScores.add(new TotalDependencyScore(dep.getId(), dep.getFromid(), 
					dep.getToid(), dep.getDependency_score()));
		}

		Collections.sort(topScores);
		
		if (topScores.size()<maxResults) {
			maxResults = topScores.size();
		}
		topScores = topScores.subList(0, maxResults);
		Set<String> reqIds = new HashSet<>();
		
		List<Dependency> topDependencies = new ArrayList<>();
		for (TotalDependencyScore score : topScores) {
			topDependencies.add(detectedWithTotalScore.get(score.getDependencyId()));
			reqIds.add(score.getFromid());
			reqIds.add(score.getToid());
		}
	
		String requirementJson = mallikasService.getSelectedRequirements(reqIds);	
		
		try {
			parser = new OpenReqJSONParser(requirementJson);
			results.add("requirements", gson.toJsonTree(parser.getRequirements()));
		} catch (com.google.gson.JsonSyntaxException|java.lang.NullPointerException e) {
			results.add("requirements", new JsonArray());
		}
		results.add("dependencies", gson.toJsonTree(topDependencies));
		
		return new ResponseEntity<>(results.toString(), HttpStatus.OK);
	}
	
	public ResponseEntity<String> updateProposed(String dependenciesJson) throws NestedServletException {
		try {
			Type depListType = new TypeToken<List<Dependency>>(){}.getType();
			List<Dependency> dependencies = gson.fromJson(dependenciesJson, depListType);
			ResponseEntity<String> updateResponse = mallikasService.updateDependencies(dependencies, false, true);
			
			if (updateResponse.getStatusCode()!=HttpStatus.OK) {
				return new ResponseEntity<>(updateResponse.getBody(), updateResponse.getStatusCode());
			}
			
			List<Dependency> correctDependencies = mallikasService.correctIdsForDependencies(dependencies);
			String orsiResponse = detectionService.acceptedAndRejectedToORSI(correctDependencies).getBody();	
			HashMap<String, List<Dependency>> depMap = mallikasService.projectsForDependencies(correctDependencies);
			
			String mulperiResponse = "";
			
			for (String projectId : depMap.keySet()) {				
				dependencies = depMap.get(projectId);
				List<Dependency> acceptedDependencies = new ArrayList<>();
				
				for (Dependency dep : dependencies) {
					if (dep.getStatus()==Dependency_status.ACCEPTED)
						acceptedDependencies.add(dep);
				}
				
				if (!acceptedDependencies.isEmpty()) {
					JsonObject object = new JsonObject();
					Project project = new Project();
					project.setId(projectId);
					
					object.add("projects", gson.toJsonTree(Arrays.asList(project)));
					object.add("requirements", gson.toJsonTree(new ArrayList<>()));
					object.add("dependencies", gson.toJsonTree(acceptedDependencies));

					mulperiResponse += "Project " + projectId + ": " + mulperiService.sendProjectUpdatesToMulperi(
							object.toString()).getBody() + "\n";
				} else {
					mulperiResponse += "No accepted dependencies for " + projectId + "\n";
				}
			}
			
			String response = "Mallikas response: " + updateResponse.getBody() + 
					"\nOrsi response: " + orsiResponse + 
					"\nMulperi/Caas response(s):\n" + mulperiResponse + "\n";
			
			return new ResponseEntity<>(response, HttpStatus.OK);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			return new ResponseEntity<>("Error:\n" + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	public ResponseEntity<String> updateWholeProject(@RequestParam String projectId) throws Exception {
		try {
			ResponseEntity<String> response = importService.importProjectIssues(projectId, authService);
			if (response!=null && response.getStatusCode()==HttpStatus.OK) {
				return mulperiService.sendProjectToMulperi(projectId);
			}
			return response;
		} catch (HttpClientErrorException|HttpServerErrorException e) {
			return new ResponseEntity<>("Error in updating the whole project " + e.getResponseBodyAsString(), e.getStatusCode());
		}
	}
	
	public ResponseEntity<String> updateMostRecentIssuesInProject(@RequestParam List<String> projectId) throws IOException {
		try {
			return importService.importUpdatedIssues(projectId, authService);			
		} catch (HttpClientErrorException|HttpServerErrorException e) {
			return new ResponseEntity<>("Error in updating the most recent issues " + e.getResponseBodyAsString(), e.getStatusCode());
		}
	}
	
	public ResponseEntity<String> updateRecentForAllProjects() throws IOException {
		String mapString = mallikasService.getListOfProjects();
		Type mapType = new TypeToken<HashMap<String, Integer>>(){}.getType();
		HashMap<String, Integer> map = gson.fromJson(mapString, mapType);
		Set<String> ids = map.keySet();
		return updateMostRecentIssuesInProject(new ArrayList<String>(ids));
	}
	
	public ResponseEntity<String> transitiveClosureWithUpdate(String requirementId, String projectId, 
			Integer layerCount) throws Exception {
		String response = mallikasService.getSelectedRequirements(Arrays.asList(requirementId));
		if (response == null) {
			System.out.println("Updating " + projectId + " (did not find " + requirementId + ")"); 
			System.out.println(updateWholeProject(projectId));
		}
		return getTransitiveClosureOfRequirement(Arrays.asList(requirementId), layerCount);
	}
}
