package eu.openreq.milla.controllers;

import eu.openreq.milla.models.json.WeightParams;
import eu.openreq.milla.services.ImportService;
import eu.openreq.milla.services.MallikasService;
import eu.openreq.milla.services.MulperiService;
import eu.openreq.milla.services.QtService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.NestedServletException;
import springfox.documentation.annotations.ApiIgnore;

import java.io.IOException;
import java.util.List;

@RestController
public class QtController {

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


	@ApiOperation(value = "Get the transitive closure of a requirement",
			notes = "Returns the transitive closure of a given requirement up to the depth of 5. "
					+ "Can now also provide custom depth value (layerCount).",
			response = String.class)
	@ApiResponses(value = { 
			@ApiResponse(code = 200, message = "Success, returns JSON model"),
			@ApiResponse(code = 400, message = "Failure, ex. model not found"), 
			@ApiResponse(code = 409, message = "Conflict")}) 
	@GetMapping(value = "getTransitiveClosureOfRequirement")
	public ResponseEntity<String> getTransitiveClosureOfRequirement(@RequestParam List<String> requirementId, 
			@RequestParam(required = false) Integer layerCount) throws IOException {
		return qtService.getTransitiveClosureOfRequirement(requirementId, layerCount);
	}
	
	@ApiOperation(value = "Get the dependencies of a requirement", notes = "Get the dependencies of a requirement, with "
			+ "minimum score and max results as params",
			response = String.class)
	@ApiResponses(value = { 
			@ApiResponse(code = 200, message = "Success, returns JSON model"),
			@ApiResponse(code = 400, message = "Failure, ex. model not found"), 
			@ApiResponse(code = 409, message = "Conflict")}) 
	@GetMapping(value = "getDependenciesOfRequirement")
	public ResponseEntity<String> getDependenciesOfRequirement(@RequestParam String requirementId, 
			@RequestParam(required = false) Double scoreThreshold, 
			@RequestParam(required = false) Integer maxResults) throws IOException {
		return qtService.getDependenciesOfRequirement(requirementId, scoreThreshold, maxResults);
	}
	
	@ApiOperation(value = "Get consistency check for the transitive closure of a requirement", notes = "First the transitive closure is created,"
			+ " then a consistency check is performed on it. Can now also provide custom depth value (layerCount), defaults to 5.",
			response = String.class)
	@ApiResponses(value = { 
			@ApiResponse(code = 200, message = "Success, returns JSON model"),
			@ApiResponse(code = 400, message = "Failure, ex. model not found"), 
			@ApiResponse(code = 409, message = "Conflict")}) 
	@GetMapping(value = "getConsistencyCheckForRequirement")
	public ResponseEntity<String> getConsistencyCheckForRequirement(@RequestParam List<String> requirementId, 
			@RequestParam (required = false) Integer layerCount, 
			@ApiParam(name = "analysisOnly", value = "If true, only analysis of consistency is performed and diagnoses are omitted. If false, Diagnosis is performed in case of inconsistency.")
			@RequestParam(required = false) boolean analysisOnly,
			@ApiParam(name = "timeOut", value = "Time in milliseconds allowed for each diagnosis. If the timeOut is exceeded, diagnosis fails and output will include 'Timeout' and 'Timeout_msg' fields. If 0 (default), there is no timeout for diagnoses.")
			@RequestParam(required = false, defaultValue = "0") int timeOut,
			@ApiParam(name = "omitCrossProject", value = "If 'true' and 'description' field of a relationship includes 'crossProjectTrue', the relationship is not taken into account in analysis. Adds 'RelationshipsIgnored' and 'RelationshipsIgnored_msg' fields to output.")
			@RequestParam(required = false) boolean omitCrossProject,
			@ApiParam(name = "omitReqRelDiag", value = "If true, the third diagnosis (both requirements and relationships) is omitted.")
			@RequestParam(required = false) boolean omitReqRelDiag) throws IOException {
		return qtService.getConsistencyCheckForRequirement(requirementId, layerCount, 
				analysisOnly, timeOut, omitCrossProject, omitReqRelDiag);
	}

	/**
	 * Get proposed dependencies of a requirement
	 * @param requirementId
	 * @param maxResults
	 * @return
	 * @throws IOException
	 */
	@ApiOperation(value = "Get proposed dependencies of a requirement saved in Mallikas", notes = "Get the top dependencies",
			response = String.class)
	@ApiResponses(value = { 
			@ApiResponse(code = 200, message = "Success, returns JSON model"),
			@ApiResponse(code = 400, message = "Failure, ex. model not found"), 
			@ApiResponse(code = 409, message = "Conflict")}) 
	@GetMapping(value = "getProposedDependenciesOfRequirement")
	@ApiIgnore
	public ResponseEntity<String> getProposedDependenciesOfRequirement(@RequestParam List<String> requirementId, 
			@RequestParam(required = false, defaultValue = "20") Integer maxResults) throws IOException {
		return qtService.getProposedDependenciesOfRequirement(requirementId, maxResults);

	}

	/**
	 * Get proposed dependencies from detection services sorted by score
	 * @param requirementId
	 * @param maxResults
	 * @param weightParams
	 * @param extraDetectionParams
	 * @return
	 * @throws IOException
	 */
	@ApiOperation(value = "Detect and get top X proposed dependencies of a requirement", notes = "Get the top dependencies "
			+ "as proposed by all detection services", 
			response = String.class)
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Success, returns JSON model"),
			@ApiResponse(code = 400, message = "Failure, ex. model not found"), 
			@ApiResponse(code = 409, message = "Conflict")}) 
	@GetMapping(value = "getTopProposedDependenciesOfRequirement")
	public ResponseEntity<String> getTopProposedDependencies(@RequestParam List<String> requirementId,
			@RequestParam(required = false, defaultValue = "20") Integer maxResults,
			@RequestParam(required = false) boolean includeRejected,
			@RequestParam(required = false, defaultValue = "") String extraDetectionParams,
			WeightParams weightParams
			) throws IOException {
		return qtService.sumScoresAndGetTopProposed(requirementId, maxResults, includeRejected,
				extraDetectionParams, weightParams);
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
	public ResponseEntity<String> updateWholeProject(@RequestParam String projectId) throws Exception {
		return qtService.updateWholeProject(projectId);
	}
	
	
	/**
	 * Fetches recent issues of the specified project(s) from Qt Jira and sends them to Mallikas and detection services.
	 * 
	 * @param projectId
	 * @return
	 * @throws IOException
	 */
	@ApiOperation(value = "Fetch only the most recent issues of a project from Qt Jira to Mallikas, and update the "
			+ "graph in KeljuCaas", notes = "Post recent issues in project(s) to Mallikas database, detection services and KeljuCaas")
	@PostMapping(value = "updateRecentInProject")
	public ResponseEntity<String> updateMostRecentIssuesInProject(@RequestParam List<String> projectId) throws IOException {
		return qtService.updateMostRecentIssuesInProject(projectId);
	}
	
	/**
	 * Update recent for all projects at once
	 * 
	 * @return
	 * @throws NestedServletException
	 * @throws IOException
	 */
	@ApiOperation(value = "Fetch recent issues for all projects", notes = "Fetch and post recent issues in ALL Jira projects to Mallikas database "
			+ "and KeljuCaas, and various detection services",
			response = String.class)
	@PostMapping(value = "updateRecentForAllProjects")
	public ResponseEntity<String> updateRecentForAllProjects() 
			throws NestedServletException, IOException {
		return qtService.updateRecentForAllProjects();
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
	public ResponseEntity<String> updateProposedDependencies(@RequestBody String dependencies) 
			throws NestedServletException, IOException {
		return qtService.updateProposed(dependencies);
	}

	@ApiIgnore
	@PostMapping(value = "transitiveClosureWithUpdate")
	public ResponseEntity<String> transitiveClosureWithUpdate(@RequestParam String requirementId, 
			@RequestParam String projectId, @RequestParam(required = false) Integer layerCount) throws Exception {
		return qtService.transitiveClosureWithUpdate(requirementId, projectId, layerCount);
	}
}
