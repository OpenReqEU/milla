package eu.openreq.milla.controllers;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;

import eu.openreq.milla.models.json.RequestParams;
import eu.openreq.milla.services.ImportService;
import eu.openreq.milla.services.MallikasService;
import eu.openreq.milla.services.MulperiService;
import eu.openreq.milla.services.OAuthService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import springfox.documentation.annotations.ApiIgnore;

@RestController
@RequestMapping(value = "/")
public class MillaController {

	@Autowired
	ImportService importService;

	@Autowired
	MulperiService mulperiService;
	
	@Autowired
	MallikasService mallikasService;
	
	@Autowired
	OAuthService authService;	
	
	/**
	 * Fetch Requirements that are in the selected Project from Mallikas, and send
	 * to Mulperi
	 * 
	 * @param
	 * @return ResponseEntity<?>
	 * @throws IOException
	 */
	@ApiOperation(value = "Send all requirements of the selected project to Mulperi.", 
	notes = "<b>Functionality</b>: All requirements of a project, excluding their proposed dependencies, are fetched from Mallikas database "
			+ "and sent to Mulperi to construct a transitive closure. "
			+ "<br><b>Precondition</b>: The project, including its requirements, has been cached in Mallikas.<br>"
			+ "<b>Postcondition</b>: Mulperi has a transitive closure of each requirement for a project up to depth five.<br>"
			+ "<br><b>Parameter: </b>"
			+ "<br>projectId: The project id in Mallikas (e.g., QTWB).")
	@PostMapping(value = "sendProjectToMulperi")
	public ResponseEntity<String> sendProjectToMulperi(@RequestParam String projectId) throws IOException {
		return mulperiService.sendProjectToMulperi(projectId);
	}

	/**
	 * Fetch all requirements of the defined project
	 * 
	 * @param projectId
	 * @return ResponseEntity<?>
	 * @throws IOException
	 */
	@ApiOperation(value = "Fetch all requirements of a project in OpenReq JSON format.", 
			notes = "<b>Functionality</b>: Fetch all requirements of a project including their dependencies that are cached in Mallikas database in the OpenReq JSON format."
					+ "<br><b>Precondition</b>: The project has been cached in Mallikas."
					+ "<br><b>Postcondition</b>: An OpenReq JSON of the project including requirements and dependencies is produced."
					+ "<br><b>Parameter: </b>"
					+ "<br>projectId: The project id in Mallikas (e.g., QTWB)."
					+ "<br>includeProposed: Whether to fetch proposed dependencies")
	@GetMapping(value = "requirementsInProject")
	public ResponseEntity<?> getRequirementsInProject(@RequestParam String projectId, @RequestParam(required = false)
			boolean includeProposed, @RequestParam(required = false) boolean requirementsOnly) throws IOException {

		String reqsInProject = mallikasService.getAllRequirementsInProject(projectId, includeProposed, requirementsOnly);

		if (reqsInProject == null || reqsInProject.equals("")) {
			return new ResponseEntity<>("Requirements not found", HttpStatus.NOT_FOUND);
		}
		return new ResponseEntity<String>(reqsInProject, HttpStatus.OK);
	}

	/**
	 * Fetch a list of selected Requirements from Mallikas
	 * 
	 * @param ids
	 * @return ResponseEntity
	 * @throws IOException
	 */
	@ApiOperation(value = "Fetch a specified set of requirements in OpenReq JSON format.", 
	notes = "<b>Functionality</b>: Fetch specified requirements and their dependencies that are cached in Mallikas database "
			+ "in OpenReq JSON format."
			+ "<br><b>Precondition</b>: The project has been cached in Mallikas."
			+ "<br><b>Postcondition</b>: An OpenReq JSON of the requirements and their dependencies is produced."
			+ "<br><b>Parameter: </b>"
			+ "<br>ids: ids as a String array, e.g. [\"QTWB-1\", \"QTWB-2\"] ")
	@PostMapping(value = "requirementsByIds")
	public ResponseEntity<?> getRequirementsByIds(@RequestParam Collection<String> ids) throws IOException {

		String reqsWithIds = mallikasService.getSelectedRequirements(ids);

		if (reqsWithIds == null || reqsWithIds.equals("")) {
			return new ResponseEntity<>("Requirements not found", HttpStatus.NOT_FOUND);
		}
		return new ResponseEntity<String>(reqsWithIds, HttpStatus.OK);
	}
	
	/**
	 * Fetch a list of requirements from the database based on various parameters.
	 * 
	 * @param params
	 * @return ResponseEntity<String>
	 * @throws IOException
	 */
	@ApiOperation(value = "Fetch requirements based on various parameters posted in JSON format.", 
			notes = "<b>Functionality</b>: Fetch requirements based on various parameters given in JSON format."
					+ "<br><b>Precondition</b>: The project has been cached in Mallikas."
					+ "<br><b>Postcondition</b>: An OpenReq JSON of the requirements and their dependencies is produced."
					+ "<br><b>Parameter: </b>"
					+ "<br>params: RequestParams object containing various parameters to be used in database query")
	@PostMapping(value = "requirementsByParams")
	public ResponseEntity<?> getRequirementsByParams(@RequestBody RequestParams params) throws IOException {
		
		String reqsWithDependencyType = mallikasService.requestWithParams(params,
				"requirements");

		if (reqsWithDependencyType == null || reqsWithDependencyType.equals("")) {
			return new ResponseEntity<>("Search failed, requirements not found", HttpStatus.NOT_FOUND);
		}
		return new ResponseEntity<String>(reqsWithDependencyType, HttpStatus.OK);
	}

	/**
	 * Use QtJiraImporter to get the issues of a selected project in OpenReq JSON
	 * format and send them to Mallikas database.
	 * 
	 * @param projectId,
	 *            ID of the selected project
	 * @return ResponseEntity if successful
	 * @throws Exception 
	 */
	@ApiOperation(value = "Import a selected project from Qt Jira and store a cache of the project in Mallikas", 
			notes = "<b>Functionality</b>: This is the full data import from Qt Jira. "
					+ "A selected project including its issues are fetched from Qt Jira. "
					+ "The data is converted to OpenReq JSON requirements and dependencies, and OpenReq JSON project is constructed. "
					+ "The project, including its requirements and dependencies, is cached to Mallikas database"
					+ "<br><b>Postcondition</b>: The selected project is cached in Mallikas to be managed in the OpenReq infrastructure. "
					+ "The same project name (id) is used in Qt Jira and Mallikas."
					+ "<br><b>Note:</b> For update rather than full import, see \"qtJiraUpdated\", which is  more efficient for large projects.</b>"
					+ "<br><b>Parameter: </b>"
					+ "<br>projectId: The id of the project to be fetched from Qt Jira (e.g., QTWB).", 
					response = String.class)
	@ApiResponses(value = { @ApiResponse(code = 201, message = "Success, all requirements and dependencies downloaded"),
			@ApiResponse(code = 400, message = "Failure, ex. malformed JSON"),
			@ApiResponse(code = 500, message = "Failure, ex. invalid URLs") })
	@PostMapping(value = "qtJira")
	public ResponseEntity<String> importFromQtJira(@RequestParam String projectId) throws Exception {
			return importService.importProjectIssues(projectId, authService);
	}
	

	/**
	 * Fetch ALL Requirements and Dependencies from Mallikas
	 * 
	 * @return ResponseEntity<String>
	 * @throws IOException
	 */
//	@ApiOperation(value = "Fetch all requirements including their dependencies from the Mallikas database in OpenReq JSON format.", 
//			notes = "<b>Functionality</b>: Fetch all requirements from Mallikas database in OpenReq JSON requirement and dependency format. "
//					+ "Project object is not included."
//					+ "This is practically an export of the entire database and, thus, dataset. "
//					+ "In Swagger, the data might get too large to be shown in the response field."
//					+ "For large database, Milla and Mallikas java VM need extra memory."
//			+ "<br><b>Precondition</b>: There is at least one project and requirement cached in Mallikas.<br>"
//			+ "<b>Postcondition</b>: Requirements and dependencies in OpenReq JSON are fetched.<br>")
	@GetMapping(value = "allRequirements")
	@ApiIgnore
	public ResponseEntity<?> getAllRequirements() throws IOException {
		String allRequirements = mallikasService.getAllRequirements();

		if (allRequirements == null || allRequirements.equals("")) {
			return new ResponseEntity<>("Requirements not found", HttpStatus.NOT_FOUND);
		}
		return new ResponseEntity<String>(allRequirements, HttpStatus.OK);
	}
	
	
	/**
	 * Uses QtJiraImporter to get the latest updated issues of a selected project in OpenReq JSON
	 * format and sends them to Mallikas database.
	 * 
	 * @param projectId,
	 *            ID of the selected project
	 * @return ResponseEntity
	 * @throws IOException
	 */
	@ApiOperation(value = "Import updated issues from Qt Jira to Mallikas.", 
			notes = "<b>Functionality</b>: Import the updated Jira issues of a project from Qt Jira and send the issues as "
					+ "OpenReq JSON requirements and dependencies to be cached in Mallikas database"
			+ "<br><b>Precondition</b>: The project has been cached in Mallikas.<br>"
			+ "<b>Postcondition</b>: New or changed Jira issues are updated to Mallikas<br>"
			+ "<b>Exception</b>: This is not full synchronization."
			+ "<br><b>Parameter: </b>"
			+ "<br>projectId: The id of the Qt Jira project (e.g., QTWB).", 
			response = String.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Success, all updated requirements and dependencies downloaded"),
			@ApiResponse(code = 400, message = "Failure, ex. malformed JSON"),
			@ApiResponse(code = 500, message = "Failure, ex. invalid URLs") })
	@PostMapping(value = "qtJiraUpdated")
	public ResponseEntity<String> importUpdatedFromQtJira(@RequestParam List<String> projectId) throws IOException {
		return importService.importUpdatedIssues(projectId, authService);
	}
	
	
	/**
	 * Get address used in authorizing Milla for Jira
	 * 
	 * @return ResponseEntity
	 * @throws IOException
	 */
	@ApiOperation(value = "Get address used in authorizing Milla for Jira", 
			notes = "Initialize authorization process and receive Jira authorization address, where "
					+ "user has to log in to receive a secret key",
			response = String.class)
	@GetMapping(value = "getJiraAuthorizationAddress")
	public ResponseEntity<?> jiraAuthorizationAddress() {
		authService.setInitialized(true);
		try {
			String response = authService.tempTokenAuthorization();
			if(response == null) {
				return new ResponseEntity<>("Cannot initialize authorization process, address not acquired", HttpStatus.UNAUTHORIZED);
			}
			return new ResponseEntity<>(response, HttpStatus.OK);
		}
		catch(HttpClientErrorException e) {
			return new ResponseEntity<>("Cannot initialize authorization process, exception: " + e.getMessage(), HttpStatus.UNAUTHORIZED);
		}
	}
	
	/**
	 * Authorize Milla for Jira with a secret key
	 * 
	 * @return ResponseEntity
	 * @throws IOException
	 */
	@ApiOperation(value = "Authorize Milla for Jira with a secret key", 
			notes = "Use a secret key received from Jira to authorize Milla",
			response = String.class)
	@PostMapping(value = "verifyJiraAuthorization")
	public ResponseEntity<?> sendSecret(@RequestBody String secret){
		if (!authService.isInitialized()) {
			return new ResponseEntity<>("No authorization process initialized", HttpStatus.UNAUTHORIZED);
		}
		try {
			String response = authService.accessTokenAuthorization(secret);
			if(response == null) {
				return new ResponseEntity<>("Authorization failed, secret incorrect?", HttpStatus.UNAUTHORIZED);
			}
			return new ResponseEntity<>(response, HttpStatus.OK);
		}
		catch (HttpClientErrorException e) {
			return new ResponseEntity<>("Cannot authorize, exception: " + e.getMessage(), HttpStatus.UNAUTHORIZED);
		}
	}
	
	/**
	 * Test if authorized, returns some user statistics on success
	 * 
	 * @return ResponseEntity
	 * @throws IOException
	 */
	@ApiOperation(value = "Test if authorized for Jira", 
			notes = "Test if successfully authorized for Jira, returns some user statistics on success",
			response = String.class)
	@GetMapping(value = "testJiraAuthorization")
	public ResponseEntity<String> test() {
		if (!authService.isInitialized()) {
			return new ResponseEntity<String>("Not authorized for Jira", HttpStatus.UNAUTHORIZED);
		}
		String result = authService.authorizedJiraRequest("/rest/auth/latest/session");
		return new ResponseEntity<String>(result, HttpStatus.OK);
	}
	 
}
