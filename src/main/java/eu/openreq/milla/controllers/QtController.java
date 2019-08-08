package eu.openreq.milla.controllers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.NestedServletException;

import eu.openreq.milla.models.json.Dependency;
import eu.openreq.milla.models.json.RequestParams;
import eu.openreq.milla.services.QtService;
import eu.openreq.milla.services.ImportService;
import eu.openreq.milla.services.MallikasService;
import eu.openreq.milla.services.MulperiService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@RestController
public class QtController {
	
	@Value("${milla.mulperiAddress}")
	private String mulperiAddress;

	@Autowired
	QtService qtService;

	@Autowired
	MallikasService mallikasService;
	
	@Autowired
	MulperiService mulperiService;
	
	@Autowired
	ImportService importService;
	
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
		} catch (HttpClientErrorException|HttpServerErrorException e) {
			return new ResponseEntity<>("Error:\n" + e.getResponseBodyAsString(), e.getStatusCode());
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
			@RequestParam(required = false) Double scoreThreshold, 
			@RequestParam(required = false) Integer maxResults) throws IOException {
		
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
	
	@ApiOperation(value = "Get consistency check for the transitive closure of a requirement", notes = "First the transitive closure is created,"
			+ " then a consistency check is performed on it. Can now also provide custom depth value (layerCount), defaults to 5.",
			response = String.class)
	@ApiResponses(value = { 
			@ApiResponse(code = 200, message = "Success, returns JSON model"),
			@ApiResponse(code = 400, message = "Failure, ex. model not found"), 
			@ApiResponse(code = 409, message = "Conflict")}) 
	@GetMapping(value = "/getConsistencyCheckForRequirement")
	public ResponseEntity<?> getConsistencyCheckForRequirement(@RequestParam List<String> requirementId, @RequestParam
			(required = false) Integer layerCount, @RequestParam(required = false) boolean analysisOnly, 
			@RequestParam(required = false, defaultValue = "0") Integer timeOut) throws IOException {

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
	
	@ApiOperation(value = "Get top X proposed dependencies of a requirement saved in Mallikas", notes = "Get the top dependencies", 
			response = String.class)
	@ApiResponses(value = { 
			@ApiResponse(code = 200, message = "Success, returns JSON model"),
			@ApiResponse(code = 400, message = "Failure, ex. model not found"), 
			@ApiResponse(code = 409, message = "Conflict")}) 
	@GetMapping(value = "/getProposedDependenciesOfRequirement")
	public ResponseEntity<?> getProposedDependenciesOfRequirement(@RequestParam List<String> requirementId, 
			@RequestParam(required = false, defaultValue = "20") Integer maxResults) throws IOException {
		
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
	
	@ApiOperation(value = "Detect and get top X proposed dependencies of a requirement", notes = "Get the top dependencies "
			+ "as proposed by all detection services", 
			response = String.class)
	@ApiResponses(value = { 
			@ApiResponse(code = 200, message = "Success, returns JSON model"),
			@ApiResponse(code = 400, message = "Failure, ex. model not found"), 
			@ApiResponse(code = 409, message = "Conflict")}) 
	@GetMapping(value = "/getTopProposedDependenciesOfRequirement")
	public ResponseEntity<?> getTopProposedDependencies(@RequestParam List<String> requirementId, 
			@RequestParam(required = false, defaultValue = "20") Integer maxResults) throws IOException {
		
		RequestParams params = new RequestParams();
		params.setRequirementIds(requirementId);
		params.setProposedOnly(true);
		if (maxResults!=null) {
			params.setMaxDependencies(maxResults);
		}
		
		return qtService.sumScoresAndGetTopProposed(params);
	}
	

	/**
	 * Fetches specified project from Qt Jira and sends it to Mallikas. Then posts the project to Mulperi and KeljuCaas for the transitive closure.
	 * 
	 * @param projectId
	 *            Project received as a parameter
	 * @return ResponseEntity<?>
	 * @throws Exception 
	 */
	@ApiOperation(value = "Fetch whole project from Qt Jira to Mallikas and update the graph in KeljuCaas", 
			notes = "Post a Project to Mallikas database and KeljuCaas")
	@PostMapping(value = "updateProject")
	public ResponseEntity<?> updateWholeProject(@RequestParam String projectId) throws Exception {
		try {
			ResponseEntity<?> response = millaController.importFromQtJira(projectId);
			if (response!=null && response.getStatusCode()==HttpStatus.OK) {
				return millaController.sendProjectToMulperi(projectId);
			}
			return response;
		} catch (HttpClientErrorException|HttpServerErrorException e) {
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
	public ResponseEntity<?> updateMostRecentIssuesInProject(@RequestParam String projectId) throws IOException {
		try {
			ResponseEntity<?> response = millaController.importUpdatedFromQtJira(projectId);			
			if (response!=null) {
				return mulperiService.postToMulperi(response.getBody(), "/models/updateMurmeliModelInKeljuCaas");
			}
			return response;
		} catch (HttpClientErrorException|HttpServerErrorException e) {
			return new ResponseEntity<>("Error in updating the most recent issues " + e.getResponseBodyAsString(), e.getStatusCode());
		}
	}
	
	/**
	 * Updates the type and status of the proposed dependencies provided
	 * 
	 * @param dependencies
	 * @return ResponseEntity
	 * @throws IOException
	 * @throws NestedServletException 
	 */
	@ApiOperation(value = "Update proposed dependencies by user input", notes = "Update proposed dependencies, were they accepted or rejected? "
			+ "If accepted, what is the type?",
			response = String.class)
	@ApiResponses(value = { 
			@ApiResponse(code = 200, message = "Success, returns JSON model"),
			@ApiResponse(code = 400, message = "Failure, ex. model not found"), 
			@ApiResponse(code = 409, message = "Conflict")}) 
	@PostMapping(value = "updateProposedDependencies")
	public ResponseEntity<String> updateProposedDependencies(@RequestBody List<Dependency> dependencies) throws NestedServletException, IOException {
		return qtService.updateProposed(dependencies);
	}

}
