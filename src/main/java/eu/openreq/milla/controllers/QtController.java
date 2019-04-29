package eu.openreq.milla.controllers;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpStatus;
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
	
	@Value("${milla.mulperiAddress}")
	private String mulperiAddress;

	@Autowired
	MallikasService mallikasService;
	
	@Autowired
	MillaController millaController;
	
	@Autowired
	RestTemplate rt;
	
	@ApiOperation(value = "Get the transitive closure of a requirement",
			notes = "Returns the transitive closure of a given requirement up to the depth of 5. "
					+ "Can now also provide custom depth value (layerCount).",
			response = String.class)
	@ApiResponses(value = { 
			@ApiResponse(code = 200, message = "Success, returns JSON model"),
			@ApiResponse(code = 400, message = "Failure, ex. model not found"), 
			@ApiResponse(code = 409, message = "Conflict")}) 
	@GetMapping(value = "/getTransitiveClosureOfRequirement")
	public ResponseEntity<?> getTransitiveClosureOfRequirement(@RequestParam List<String> requirementId, 
			@RequestParam(required = false) Integer layerCount) throws IOException {

		String completeAddress = mulperiAddress + "/models/findTransitiveClosureOfRequirement";
		
		if (layerCount!=null) {
			completeAddress += "?layerCount=" + layerCount;
		}
				
		try {
			String response = rt.postForObject(completeAddress, requirementId, String.class);		
			return new ResponseEntity<>(response, HttpStatus.OK);
		} catch (HttpClientErrorException e) {
			return new ResponseEntity<>("Error:\n\n" + e.getResponseBodyAsString(), e.getStatusCode());
		}
		catch (Exception e) {
			return new ResponseEntity<>("Error: " + e.getMessage(), HttpStatus.BAD_REQUEST);
		}

	}
	
	@ApiOperation(value = "Get the dependencies of a requirement", notes = "Get the dependencies of a requirement, with "
			+ "minimum score and max results as params",
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

		String reqsWithDependencyType = mallikasService.requestWithParams(params, "dependencies");

		if (reqsWithDependencyType == null || reqsWithDependencyType.equals("")) {
			return new ResponseEntity<>("Search failed, requirements not found \n\n", HttpStatus.NOT_FOUND);
		}
		return new ResponseEntity<>(reqsWithDependencyType, HttpStatus.FOUND);
		

	}
	
	@ApiOperation(value = "Get consistency check for the transitive closure of a requirement", notes = "First the transitive closure is created, then"
			+ "a consistency check is performed on it. Can now also provide custom depth value (layerCount), defaults to 5.",
			response = String.class)
	@ApiResponses(value = { 
			@ApiResponse(code = 200, message = "Success, returns JSON model"),
			@ApiResponse(code = 400, message = "Failure, ex. model not found"), 
			@ApiResponse(code = 409, message = "Conflict")}) 
	@GetMapping(value = "/getConsistencyCheckForRequirement")
	public ResponseEntity<?> getConsistencyCheckForRequirement(@RequestParam String requirementId, @RequestParam
			(required = false) Integer layerCount) throws IOException {
		String completeAddress = mulperiAddress + "/models/consistencyCheckForTransitiveClosure";
		
		if (layerCount!=null) {
			completeAddress += "?layerCount=" + layerCount;
		}
		
		try {
			String response = rt.postForObject(completeAddress, requirementId, String.class);
			return new ResponseEntity<>(response, HttpStatus.FOUND);
		} catch (HttpClientErrorException e) {
			return new ResponseEntity<>("Error:\n\n" + e.getResponseBodyAsString(), e.getStatusCode());
		}
		catch (Exception e) {
			return new ResponseEntity<>("Error: " + e.getMessage(), HttpStatus.BAD_REQUEST);
		}
		

	}
	
	@ApiOperation(value = "Get top X proposed dependencies of a requirement", notes = "Get the top dependencies as proposed by detection services", 
			response = String.class)
	@ApiResponses(value = { 
			@ApiResponse(code = 200, message = "Success, returns JSON model"),
			@ApiResponse(code = 400, message = "Failure, ex. model not found"), 
			@ApiResponse(code = 409, message = "Conflict")}) 
	@GetMapping(value = "/getTopProposedDependenciesOfRequirement")
	public ResponseEntity<?> getTopProposedDependenciesOfRequirement(@RequestParam List<String> requirementId, @RequestParam Integer maxResults) throws IOException {
		
		RequestParams params = new RequestParams();
		params.setRequirementIds(requirementId);
		params.setProposedOnly(true);
		params.setMaxDependencies(maxResults);
		
		String reqWithTopProposed = mallikasService.requestWithParams(params,
				"dependencies");

		if (reqWithTopProposed == null || reqWithTopProposed.equals("")) {
			return new ResponseEntity<>("Search failed, requirements not found \n\n", HttpStatus.NOT_FOUND);
		}
		return new ResponseEntity<>(reqWithTopProposed, HttpStatus.FOUND);

	}
	

	/**
	 * Fetches specified project from Qt Jira and sends it to Mallikas. Then posts the project to Mulperi and KeljuCaas for the transitive closure.
	 * 
	 * @param projectId
	 *            Project received as a parameter
	 * @return ResponseEntity<?>
	 * @throws IOException
	 * @throws NoSuchAlgorithmException 
	 * @throws InvalidKeySpecException 
	 */
	@ApiOperation(value = "Fetch whole project from Qt Jira to Mallikas and update the graph in KeljuCaas", 
			notes = "Post a Project to Mallikas database and KeljuCaas")
	@PostMapping(value = "updateProject")
	public ResponseEntity<?> updateWholeProject(@RequestBody String projectId) throws IOException, InvalidKeySpecException, NoSuchAlgorithmException {
		try {
			ResponseEntity<?> response = millaController.importFromQtJira(projectId);
			if (response!=null && response.getStatusCode()==HttpStatus.OK) {
				return millaController.sendProjectToMulperi(projectId);
			}
			return response;
		} catch (HttpClientErrorException e) {
			return new ResponseEntity<>("Error in updating the whole project " + e.getResponseBodyAsString(), e.getStatusCode());
		}
	}
	
	
	/**
	 * Fetches recent issues of the specified project from Qt Jira and sends them to Mallikas.
	 * 
	 * @param projectId
	 *            Project received as a parameter
	 * @return ResponseEntity<?>
	 * @throws IOException
	 */
	@ApiOperation(value = "Fetch only the most recent issues of a project from Qt Jira to Mallikas and update the "
			+ "graph in KeljuCaas", notes = "Post recent issues in a project to Mallikas database and KeljuCaas")
	@PostMapping(value = "updateRecentInProject")
	public ResponseEntity<?> updateMostRecentIssuesInProject(@RequestBody String projectId) throws IOException {
		try {
			ResponseEntity<?> response = millaController.importUpdatedFromQtJira(projectId);
			if (response!=null) {
				return millaController.sendProjectToMulperi(projectId);
			}
			return response;
		} catch (HttpClientErrorException e) {
			return new ResponseEntity<>("Error in updating the most recent issues " + e.getResponseBodyAsString(), e.getStatusCode());
		}
	}
	
	/**
	 * Updates the type and status of the proposed dependencies provided
	 * 
	 * @param dependencies
	 * @return ResponseEntity
	 * @throws IOException
	 */
	@ApiOperation(value = "Update proposed dependencies by user input", notes = "Update proposed dependencies, were they accepted or rejected? "
			+ "If accepted, what is the type?",
			response = String.class)
	@ApiResponses(value = { 
			@ApiResponse(code = 200, message = "Success, returns JSON model"),
			@ApiResponse(code = 400, message = "Failure, ex. model not found"), 
			@ApiResponse(code = 409, message = "Conflict")}) 
	@PostMapping(value = "updateProposedDependencies")
	public ResponseEntity<?> updateProposedDependencies(@RequestBody String dependencies) throws IOException {
		try {
			String updated = mallikasService.convertAndUpdateDependencies(dependencies, false, true);
			return new ResponseEntity<>(updated, HttpStatus.OK);
		} catch (HttpClientErrorException e) {
			return new ResponseEntity<>("Mallikas error:\n\n" + e.getResponseBodyAsString(), e.getStatusCode());
		}

	}

}
