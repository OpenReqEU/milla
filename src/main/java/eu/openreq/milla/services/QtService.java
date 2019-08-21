package eu.openreq.milla.services;

import java.io.IOException;
import java.util.ArrayList;
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
import com.google.gson.JsonObject;

import eu.openreq.milla.models.TotalDependencyScore;
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
			return new ResponseEntity<>("Error:\n" + e.getResponseBodyAsString(), e.getStatusCode());
		}
	}
	
	public ResponseEntity<?> getDependenciesOfRequirement(String requirementId, 
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
	
	public ResponseEntity<?> getConsistencyCheckForRequirement(List<String> requirementId,
			Integer layerCount, boolean analysisOnly, Integer timeOut) throws IOException {

		String completeAddress = mulperiAddress + "/models/consistencyCheckForTransitiveClosure?analysisOnly=" + analysisOnly + 
				"&timeOut=" + timeOut;
		
		if (layerCount!=null) {
			completeAddress += "&layerCount=" + layerCount;
		}
		
		try {
			String response = rt.postForObject(completeAddress, requirementId, String.class);
			return new ResponseEntity<>(response, HttpStatus.OK);
		} catch (HttpClientErrorException|HttpServerErrorException e) {
			return new ResponseEntity<>("Error:\n\n" + e.getResponseBodyAsString(), e.getStatusCode());
		}
	}
	
	public ResponseEntity<?> getProposedDependenciesOfRequirement(List<String> requirementId, 
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
	 * @param requirementId
	 * @param maxResults
	 * @return
	 * @throws IOException
	 */
	public ResponseEntity<String> sumScoresAndGetTopProposed(List<String> requirementId, 
			Integer maxResults) throws IOException {
		
		RequestParams params = new RequestParams();
		params.setRequirementIds(requirementId);
		params.setProposedOnly(false);
		params.setIncludeRejected(true);
		if (maxResults!=null) {
			params.setMaxDependencies(maxResults);
		}
		
		List<Dependency> proposed = new ArrayList<Dependency>();		
		List<String> acceptedAndRejectedIds = new ArrayList<String>();
		
		String detectedFromMallikasString = mallikasService.requestWithParams(params,
				"dependencies");
		
		if (detectedFromMallikasString==null) {
			return new ResponseEntity<String>("Search failed, no requirements found", HttpStatus.NOT_FOUND);
		}
		
		//Get from Mallikas
		
		OpenReqJSONParser parser = null;
		try {
			parser = new OpenReqJSONParser(detectedFromMallikasString);
			List<Dependency> detectedFromMallikas = parser.getDependencies();
			for (Dependency dep : detectedFromMallikas) {
				if (dep.getStatus()==Dependency_status.PROPOSED) {
					proposed.add(dep);
				} else{
					acceptedAndRejectedIds.add(dep.getId());
				}
			}
	        
		} catch (com.google.gson.JsonSyntaxException e) {
			System.out.println("Error when parsing dependencies saved in Mallikas " + e.getMessage());
		}
		
		//Get from services
		
		for (String reqId : params.getRequirementIds()) {
			List<Dependency> detectedFromServices = detectionService.getDetectedFromServices(reqId);
			for (Dependency dep : detectedFromServices) {
				if (!acceptedAndRejectedIds.contains(dep.getId()) && 
						dep.getStatus()==Dependency_status.PROPOSED)
					proposed.add(dep);	
			}
		}
		
		
		if (proposed.isEmpty()) {
			return new ResponseEntity<String>(detectedFromMallikasString, HttpStatus.OK);
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
					Set<String> desc = new HashSet<String>(totalDep.getDescription());
					desc.addAll(dep.getDescription());
					totalDep.setDescription(new ArrayList<String>(desc));
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
		
		JsonObject results = new JsonObject();
		results.add("dependencies", new Gson().toJsonTree(topDependencies));
		
		String requirementJson = mallikasService.getSelectedRequirements(reqIds);
		
		try {
			parser = new OpenReqJSONParser(requirementJson);
			results.add("requirements", new Gson().toJsonTree(parser.getRequirements()));
		} catch (com.google.gson.JsonSyntaxException e) {
			System.out.println("Couldn't get requirements from Mallikas");
		}
		
		return new ResponseEntity<>(results.toString(), HttpStatus.OK);
	}
	
	public ResponseEntity<String> updateProposed(List<Dependency> dependencies) throws IOException, NestedServletException {
		try {
			String updated = mallikasService.updateDependencies(dependencies, false, true);
			ResponseEntity<String> orsiResponse = detectionService.acceptedAndRejectedToORSI(dependencies);
			System.out.println(orsiResponse);
			return new ResponseEntity<>(updated, HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<>("Error:\n\n" + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	public ResponseEntity<?> updateWholeProject(@RequestParam String projectId) throws Exception {
		try {
			ResponseEntity<?> response = importService.importProjectIssues(projectId, authService);
			if (response!=null && response.getStatusCode()==HttpStatus.OK) {
				return mulperiService.postToMulperi(projectId, "/models/murmeliModelToKeljuCaas");
			}
			return response;
		} catch (HttpClientErrorException|HttpServerErrorException e) {
			return new ResponseEntity<>("Error in updating the whole project " + e.getResponseBodyAsString(), e.getStatusCode());
		}
	}
}
