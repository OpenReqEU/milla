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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.util.NestedServletException;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import eu.openreq.milla.models.TotalDependencyScore;
import eu.openreq.milla.models.json.Dependency;
import eu.openreq.milla.models.json.RequestParams;

@Service
public class QtService {
	
	@Autowired
	DetectionService detectionService;

	@Autowired
	MallikasService mallikasService;
	
	public ResponseEntity<String> sumScoresAndGetTopProposed(RequestParams params) throws IOException {
		
		List<Dependency> detected = new ArrayList<Dependency>();		
		
		for (String reqId : params.getRequirementIds()) {
			detected.addAll(detectionService.getDetectedFromServices(reqId));
		}
		
		String proposedFromMallikas = mallikasService.requestWithParams(params,
				"dependencies");
		
		OpenReqJSONParser parser = null;
		
		if (proposedFromMallikas==null) {
			return new ResponseEntity<String>("Search failed, no requirements found", HttpStatus.NOT_FOUND);
		}
		
		try {
			parser = new OpenReqJSONParser(proposedFromMallikas);
	        detected.addAll(parser.getDependencies());
		} catch (com.google.gson.JsonSyntaxException e) {
			System.out.println("No dependencies saved in Mallikas");
		}
		
		if (detected.isEmpty()) {
			return new ResponseEntity<String>(proposedFromMallikas, HttpStatus.OK);
		}
		
		//Sum the scores
		
		Map<String, Dependency> detectedWithTotalScore = new HashMap<>();
		List<TotalDependencyScore> topScores = new ArrayList<>();
		
		for (Dependency dep : detected) {
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
		
		int maxResults = params.getMaxDependencies();
		
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
}
