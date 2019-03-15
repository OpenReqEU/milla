package eu.openreq.milla.controllers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;


import eu.openreq.milla.models.json.RequestParams;
import eu.openreq.milla.services.MallikasService;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@SpringBootApplication
@RestController
public class QtController {
	
	@Value("${milla.mallikasAddress}")
	private String mallikasAddress;
	
	@Value("${milla.mulperiAddress}")
	private String mulperiAddress;

	@Autowired
	MallikasService mallikasService;
	
	@Autowired
	MillaController millaController;
	
	@ApiOperation(value = "Get the transitive closure of a requirement",
			notes = "Returns the transitive closure of a given requirement to the depth of 5",
			response = String.class)
	@ApiResponses(value = { 
			@ApiResponse(code = 200, message = "Success, returns JSON model"),
			@ApiResponse(code = 400, message = "Failure, ex. model not found"), 
			@ApiResponse(code = 409, message = "Conflict")}) 
	@GetMapping(value = "/getTransitiveClosureOfRequirement")
	public ResponseEntity<?> getTransitiveClosureOfRequirement(@RequestParam String requirementId) throws IOException {
		RestTemplate rt = new RestTemplate();

		String completeAddress = mulperiAddress + "/models/findTransitiveClosureOfRequirement";
		
		String response = null;
		try {
			response = rt.postForObject(completeAddress, requirementId, String.class);		
			return new ResponseEntity<>(response, HttpStatus.OK);
		} catch (HttpClientErrorException e) {
			return new ResponseEntity<>("Error:\n\n" + e.getResponseBodyAsString(), e.getStatusCode());
		}
		catch (Exception e) {
			return new ResponseEntity<>("Error: " + e.getMessage(), HttpStatus.BAD_REQUEST);
		}

	}
	
	@ApiOperation(value = "Get the dependencies of a requirement",
			response = String.class)
	@ApiResponses(value = { 
			@ApiResponse(code = 200, message = "Success, returns JSON model"),
			@ApiResponse(code = 400, message = "Failure, ex. model not found"), 
			@ApiResponse(code = 409, message = "Conflict")}) 
	@GetMapping(value = "/getDependenciesOfRequirement")
	public ResponseEntity<?> getDependenciesOfRequirement(@RequestParam String requirementId, 
			@RequestParam(required = false) Double scoreThreshold, @RequestParam(required = false) Integer maxResults) throws IOException {
		
		RequestParams params = new RequestParams();
		List<String> reqIds = new ArrayList<String>();
		reqIds.add(requirementId);
		params.setRequirementIds(reqIds);
		params.setScoreThreshold(scoreThreshold);
		params.setMaxDependencies(maxResults);
		
		String completeAddress = mallikasAddress + "/onlyDependenciesByParams";

		String reqsWithDependencyType = mallikasService.sendRequestWithParamsToMallikas(params,
				completeAddress);

		if (reqsWithDependencyType == null || reqsWithDependencyType.equals("")) {
			return new ResponseEntity<>("Search failed, requirements not found \n\n", HttpStatus.NOT_FOUND);
		}
		ResponseEntity<String> response = new ResponseEntity<>(reqsWithDependencyType, HttpStatus.FOUND);
		return response;

	}
	
	@ApiOperation(value = "Get concistency check for the transitive closure of a requirement",
			response = String.class)
	@ApiResponses(value = { 
			@ApiResponse(code = 200, message = "Success, returns JSON model"),
			@ApiResponse(code = 400, message = "Failure, ex. model not found"), 
			@ApiResponse(code = 409, message = "Conflict")}) 
	@GetMapping(value = "/getConsistencyCheckForRequirement")
	public ResponseEntity<?> getConsistencyCheckForRequirement(@RequestParam String requirementId) throws IOException {
		
		RestTemplate rt = new RestTemplate();
		
		String completeAddress = mulperiAddress + "/models/consistencyCheckForTransitiveClosure";

		String response = null;
		try {
			response = rt.postForObject(completeAddress, requirementId, String.class);
		} catch (HttpClientErrorException e) {
			return new ResponseEntity<>("Error:\n\n" + e.getResponseBodyAsString(), e.getStatusCode());
		}
		catch (Exception e) {
			return new ResponseEntity<>("Error: " + e.getMessage(), HttpStatus.BAD_REQUEST);
		}

		return new ResponseEntity<>(response, HttpStatus.FOUND);

	}
	
	@ApiOperation(value = "Get top X proposed dependencies of a requirement",
			response = String.class)
	@ApiResponses(value = { 
			@ApiResponse(code = 200, message = "Success, returns JSON model"),
			@ApiResponse(code = 400, message = "Failure, ex. model not found"), 
			@ApiResponse(code = 409, message = "Conflict")}) 
	@GetMapping(value = "/getTopProposedDependenciesOfRequirement")
	public ResponseEntity<?> getTopProposedDependenciesOfRequirement(@RequestParam List<String> requirementId, @RequestParam Integer maxResults) throws IOException {
		
		String completeAddress = mallikasAddress + "/onlyDependenciesByParams";
		
		RequestParams params = new RequestParams();
		params.setRequirementIds(requirementId);
		params.setProposedOnly(true);
		params.setMaxDependencies(maxResults);
		
		String reqWithTopProposed = mallikasService.sendRequestWithParamsToMallikas(params,
				completeAddress);

		if (reqWithTopProposed == null || reqWithTopProposed.equals("")) {
			return new ResponseEntity<>("Search failed, requirements not found \n\n", HttpStatus.NOT_FOUND);
		}
		return new ResponseEntity<>(reqWithTopProposed, HttpStatus.FOUND);

	}
	

	/**
	 * Fetches specified project from Qt Jira and sends it to Mallikas. Then posts the project to Mulperi and KeljuCaas for the transitive closure.
	 * 
	 * @param project
	 *            Project received as a parameter
	 * @return ResponseEntity<?>
	 * @throws IOException
	 */
	@ApiOperation(value = "Fetch whole project from Qt Jira to Mallikas and update the graph in KeljuCaas", notes = "Post a Project to Mallikas database and KeljuCaas")
	//@ResponseBody
	@PostMapping(value = "updateProject")
	public ResponseEntity<?> updateWholeProject(@RequestBody String projectId) throws IOException {

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		ResponseEntity<?> response = null;

		try {
			response = millaController.importFromQtJira(projectId);
			if(response!=null) {
				return millaController.sendProjectToMulperi(projectId);
			}
		} catch (HttpClientErrorException e) {
			return new ResponseEntity<>("Error in updating the whole project " + e.getResponseBodyAsString(), e.getStatusCode());
		}
		return response;
	}
	
	
	/**
	 * Fetches specified project from Qt Jira and sends it to Mallikas. Then posts the project to Mulperi and KeljuCaas for the transitive closure.
	 * 
	 * @param project
	 *            Project received as a parameter
	 * @return ResponseEntity<?>
	 * @throws IOException
	 */
	@ApiOperation(value = "Fetch only the most recent issues of a project from Qt Jira to Mallikas and update the "
			+ "graph in KeljuCaas", notes = "Post recent issues in a project to Mallikas database and KeljuCaas")
	@PostMapping(value = "updateRecentInProject")
	public ResponseEntity<?> updateTheMostRecentIssuesInProject(@RequestBody String projectId) throws IOException {

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		ResponseEntity<?> response = null;

		try {
			response = millaController.importUpdatedFromQtJira(projectId);
		} catch (HttpClientErrorException e) {
			return new ResponseEntity<>("Error in updating the most recent issues " + e.getResponseBodyAsString(), e.getStatusCode());
		}
		return response;
	}
	
	/**
	 * Updates the type and status of the proposed dependencies provided
	 * 
	 * @param dependencies
	 * @return
	 * @throws IOException
	 */
	@ApiOperation(value = "Update proposed depencies (were they accepted or rejected?)",
			response = String.class)
	@ApiResponses(value = { 
			@ApiResponse(code = 200, message = "Success, returns JSON model"),
			@ApiResponse(code = 400, message = "Failure, ex. model not found"), 
			@ApiResponse(code = 409, message = "Conflict")}) 
	@PostMapping(value = "updateProposedDependencies")
	public ResponseEntity<?> updateProposedDependencies(@RequestBody String dependencies) throws IOException {
		
		String completeAddress = mallikasAddress + "/updateDependencies?userInput=true";

		String updated = null;
		
		try {
			updated = mallikasService.updateSelectedDependencies(dependencies, completeAddress, true);

		} catch (HttpClientErrorException e) {
			return new ResponseEntity<>("Mallikas error:\n\n" + e.getResponseBodyAsString(), e.getStatusCode());
		}
		ResponseEntity<String> response = new ResponseEntity<>(updated, HttpStatus.OK);
		return response;
	}

}
