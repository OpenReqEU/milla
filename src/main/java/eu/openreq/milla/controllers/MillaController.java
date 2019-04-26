package eu.openreq.milla.controllers;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.google.gson.JsonElement;

import eu.openreq.milla.models.jira.Issue;
import eu.openreq.milla.models.json.Dependency;
import eu.openreq.milla.models.json.Person;
import eu.openreq.milla.models.json.Project;
import eu.openreq.milla.models.json.RequestParams;
import eu.openreq.milla.models.json.Requirement;
import eu.openreq.milla.services.FormatTransformerService;
import eu.openreq.milla.services.MallikasService;
import eu.openreq.milla.services.OAuthService;
import eu.openreq.milla.services.UpdateService;
import eu.openreq.milla.qtjiraimporter.ProjectIssues;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import springfox.documentation.annotations.ApiIgnore;

@SpringBootApplication
@RestController
@RequestMapping(value = "/")
public class MillaController {

	@Value("${milla.mulperiAddress}")
	private String mulperiAddress;
	
	@Value("${jira.username}")
	private String jiraUsername;
	
	@Value("${jira.password}")
	private String jiraPassword;

	@Autowired
	FormatTransformerService transformer;

	@Autowired
	MallikasService mallikasService;
	
	@Autowired
	UpdateService updateService;
	
	OAuthService authService;
	

	/**
	 * Post Requirements and Dependencies to Mulperi.
	 * 
	 * @param data
	 * @param path
	 * @return
	 * @throws IOException
	 */
	private ResponseEntity<?> postToMulperi(@RequestBody String data) throws IOException {
		
		RestTemplate rt = new RestTemplate();
		
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		String completeAddress = mulperiAddress + "/models/requirementsToChoco";

		HttpEntity<String> entity = new HttpEntity<String>(data, headers);
		try {
			return rt.postForEntity(completeAddress, entity, String.class);
		} catch (HttpClientErrorException e) {
			return new ResponseEntity<>(e.getResponseBodyAsString(), e.getStatusCode());
		}

	}

//	/**
//	 * Fetch Requirements by params given from Mallikas, and send
//	 * to Mulperi
//	 * 
//	 * @param
//	 * @return ResponseEntity<?>
//	 * @throws IOException
//	 */
//	@ApiOperation(value = "Construct a transitive closure by sending all requirements of the selected project to Mulperi", 
//	notes = "<b>Functionality</b>: All requirements of a project are fetched from Mallikas database and sent to Mulperi to construct a transitive closure."
//			+ "<br><b>Precondition</b>: The project, including its requirements, has been cached in Mallikas.<br>"
//			+ "<b>Postcondition</b>: Mulperi has a transitive closure of each requirement for a project up to depth five.<br>"
//			+ "<br><b>Parameters: </b>"
//			+ "<br>projectId: The project id in Mallikas (e.g., QTWB)."
//			+ "<br>includeProposedDependencies: Whether to fetch dependencies with the status 'proposed'")
//	@PostMapping(value = "sendProjectToMulperiWithParams")
//	public ResponseEntity<?> sendProjectToMulperiWithParams(@RequestParam String projectId, @RequestParam boolean includeProposedDependencies) throws IOException {
//
//		RequestParams params = new RequestParams();
//		
//		params.setProjectId(projectId);
//		params.setIncludeProposed(includeProposedDependencies);
//		
//		String reqsInProject = mallikasService.requestWithParams(params, "requirements");
//
//		if (reqsInProject == null) {
//			return new ResponseEntity<>("Requirements not found \n\n", HttpStatus.NOT_FOUND);
//		}
//		return this.postToMulperi(reqsInProject);
//	}
	
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
	public ResponseEntity<?> sendProjectToMulperi(@RequestParam String projectId) throws IOException {

		String reqsInProject = mallikasService.getAllRequirementsInProject(projectId, false);

		if (reqsInProject == null) {
			return new ResponseEntity<>("Requirements not found \n\n", HttpStatus.NOT_FOUND);
		}
		return this.postToMulperi(reqsInProject);
	}

	/**
	 * Post a Collection of OpenReq JSON Requirements to Mallikas database
	 * 
	 * @param requirements
	 *            Collection<Requirement> received as a parameter, requirements to
	 *            be saved into the database
	 * @return ResponseEntity<?>
	 * @throws IOException
	 */
//	@ApiOperation(value = "Store (TBD or update?) requirements to Mallikas.", 
//	notes = "<b>Functionality</b>: Add or update an array of requirements in OpenReq JSON format in Mallikas database. "
//			+ "<br><b>Postcondition</b>: Requirements are stored in Mallikas."
//			+ "<br><b>Note: </b> The project needs to be updated separately to contain references to the new requirements."
//			+ "<br><b>Parameter: </b>"
//			+ "<br>requirements: An array of requirements in OpenReq JSON format.")
//	@PostMapping(value = "requirements")
	private ResponseEntity<?> postRequirementsToMallikas(@RequestBody Collection<Requirement> requirements)
			throws IOException {
		try {
			mallikasService.updateRequirements(requirements);
			return new ResponseEntity<String>("Mallikas update successful\n\n", HttpStatus.OK);

		} catch (HttpClientErrorException e) {
			return new ResponseEntity<>("Mallikas error:\n\n" + e.getResponseBodyAsString(), e.getStatusCode());
		}
	}

	/**
	 * Post a Collection of OpenReq JSON Dependencies to Mallikas database
	 * 
	 * @param dependencies
	 *            Collection<Dependency> received as a parameter, requirements to be
	 *            saved into the database
	 * @param isProposed           
	 * @return ResponseEntity<?>
	 * @throws IOException
	 */
//	@ApiOperation(value = "Store (TBD or update?) dependencies to Mallikas", 
//			notes = "<br><b>Functionality</b>: Add or update a set of dependencies as an array of dependencies in OpenReq JSON format. The dependencies are for the existing requirements in the Mallikas database."
//					+ "<br><b>Postcondition</b>: The dependencies are added or updated to the existing requirements in Mallikas. If a dependency exist, it is updated."
//					+ "<br><b>Parameters: </b>"
//					+ "<br>dependencies: An array of dependencies in OpenReq JSON format."
//					+ "<br>isProposed: Whether the dependencies being sent are proposed dependencies.")
//	@PostMapping(value = "dependencies")
	public ResponseEntity<?> postDependenciesToMallikas(@RequestBody Collection<Dependency> dependencies, 
			@RequestParam(required = false) boolean isProposed)
			throws IOException {
		try {
			mallikasService.updateDependencies(dependencies, isProposed, false);
			return new ResponseEntity<String>("Mallikas update successful\n\n", HttpStatus.OK);
			
		} catch (HttpClientErrorException e) {
			return new ResponseEntity<>("Mallikas error:\n\n" + e.getResponseBodyAsString(), e.getStatusCode());
		}
	}
	
	/**
	 * Post an OpenReq JSON Project to Mallikas database
	 * 
	 * @param project
	 *            Project received as a parameter
	 * @return ResponseEntity<?>
	 * @throws IOException
	 */
//	@ApiOperation(value = "Store a project in Mallikas", 
//			notes = "<br><b>Functionality</b>: Add or update a project in Mallikas. The project is in OpenReq JSON format primarily containing the IDs of requirements in the project. "
//			+ "If the projects already exist in Mallikas, the existing data is updated."
//			+ "<br><b>Postcondition</b>: The project is added or updated in Mallikas."
//			+ "<br><b>Exception</b>: TBD: What is a requirement is removed from a project, does this remove it?"
//			+ "<br><b>Parameter: </b>"
//			+ "<br>project: A project in OpenReq JSON.")
//	//@ResponseBody
//	@PostMapping(value = "project")
	private ResponseEntity<?> postProjectToMallikas(@RequestBody Project project) throws IOException {
		try {
			mallikasService.postProject(project);
			return new ResponseEntity<String>("Mallikas update successful\n\n", HttpStatus.OK);
		} catch (HttpClientErrorException e) {
			return new ResponseEntity<>("Mallikas error:\n\n" + e.getResponseBodyAsString(), e.getStatusCode());
		}
	}

	/**
	 * Fetch all requirements of the project defined
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
			boolean includeProposed) throws IOException {

		String reqsInProject = mallikasService.getAllRequirementsInProject(projectId, includeProposed);

		if (reqsInProject == null || reqsInProject.equals("")) {
			return new ResponseEntity<>("Requirements not found \n\n", HttpStatus.OK);
		}
		return new ResponseEntity<String>(reqsInProject, HttpStatus.FOUND);
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
	public ResponseEntity<?> getRequirementsByIds(@RequestBody Collection<String> ids) throws IOException {

		String reqsWithIds = mallikasService.getSelectedRequirements(ids);

		if (reqsWithIds == null || reqsWithIds.equals("")) {
			return new ResponseEntity<>("Requirements not found \n\n", HttpStatus.OK);
		}
		return new ResponseEntity<String>(reqsWithIds, HttpStatus.FOUND);
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
			return new ResponseEntity<>("Search failed, requirements not found \n\n", HttpStatus.OK);
		}
		return new ResponseEntity<String>(reqsWithDependencyType, HttpStatus.FOUND);
	}

	/**
	 * Use QtJiraImporter to get the issues of a selected project in OpenReq JSON
	 * format and send them to Mallikas database.
	 * 
	 * @param projectId,
	 *            ID of the selected project
	 * @return ResponseEntity if successful
	 * @throws IOException
	 * @throws NoSuchAlgorithmException 
	 * @throws InvalidKeySpecException 
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
	public ResponseEntity<?> importFromQtJira(@RequestBody String projectId) throws IOException, InvalidKeySpecException, NoSuchAlgorithmException {
		
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
				this.postRequirementsToMallikas(requirements);
				this.postDependenciesToMallikas(dependencies, false);
				projectIssuesAsJson.clear();
				issues.clear();
				requirements.clear();
				dependencies.clear();
				System.out.println("End is " + end);
				start = end + 1;
				end = end + divided;
			}

			Project project = transformer.createProject(projectId, requirementIds);
			this.postProjectToMallikas(project);

			// System.out.println("Epic count is " + epicCount);
			// System.out.println("Subtask count is " + subtaskCount);
			
			long end1 = System.nanoTime();
			long durationSec = (end1 - start1) / 1000000000;
			double durationMin = durationSec / 60.0;
			System.out.println("Download done, it took " + durationSec + " second(s) or " + durationMin + " minute(s).");

			return new ResponseEntity<String>("All requirements and dependencies downloaded",
					HttpStatus.OK);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new ResponseEntity<>("Download failed", HttpStatus.BAD_REQUEST);
	}
	
	// String might get too large for Swagger
	/**
	 * Fetch all Requirements and Dependencies from Mallikas
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
			return new ResponseEntity<>("Requirements not found \n\n", HttpStatus.OK);
		}
		return new ResponseEntity<String>(allRequirements, HttpStatus.FOUND);
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
	public ResponseEntity<?> importUpdatedFromQtJira(@RequestBody String projectId) throws IOException {
		
		Person person = new Person();
		person.setUsername("user_" + projectId);
		person.setEmail("dummyEmail");
		
		try {
			String response = mallikasService.getListOfProjects();
			if (response==null || !response.contains(projectId)) {
				Project project = transformer.createProject(projectId, new ArrayList<String>());
				mallikasService.postProject(project);
			}
			
			return updateService.getAllUpdatedIssues(projectId, person, authService);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new ResponseEntity<>("Download failed", HttpStatus.BAD_REQUEST);
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
	public ResponseEntity<?> jiraAuthorizationAddress() throws InvalidKeySpecException, NoSuchAlgorithmException, IOException {
		authService = new OAuthService();
		try {
			String response = authService.tempTokenAuthorization();
			if(response == null) {
				return new ResponseEntity<>("Authorization failed, address not acquired", HttpStatus.UNAUTHORIZED);
			}
			return new ResponseEntity<>(response, HttpStatus.OK);
		}
		catch(Exception e) {
			return new ResponseEntity<>("Cannot authorize, exception: " + e.getMessage(), HttpStatus.UNAUTHORIZED);
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
	public ResponseEntity<?> sendSecret(@RequestBody String secret) throws IOException {
		if (authService == null) {
			return new ResponseEntity<>("No authorization initialized", HttpStatus.EXPECTATION_FAILED);
		}
		try {
			String response = authService.accessTokenAuthorization(secret);
			if(response == null) {
				return new ResponseEntity<>("Authorization failed, secret incorrect?", HttpStatus.UNAUTHORIZED);
			}
			return new ResponseEntity<>(response, HttpStatus.OK);
		}
		catch (Exception e) {
			return new ResponseEntity<>("Cannot authorize, exception: " + e.getMessage(), HttpStatus.UNAUTHORIZED);
		}
	}
	
	@PostMapping(value = "testAuthorizedRequest")
	public ResponseEntity<?> authorizedRequest(@RequestParam String address) throws IOException, InvalidKeySpecException, NoSuchAlgorithmException {
		if (authService == null) {
			return new ResponseEntity<>("No authorization initialized", HttpStatus.EXPECTATION_FAILED);
		}
		try {
			String response = authService.authorizedRequest(address);
			if(response == null) {
				return new ResponseEntity<>("Authorization failed, address unauthorized", HttpStatus.UNAUTHORIZED);
			}
			return new ResponseEntity<>(response, HttpStatus.OK);
		}
		catch (Exception e) {
			return new ResponseEntity<>("Cannot authorize, exception: " + e.getMessage(), HttpStatus.UNAUTHORIZED);
		}
	}
	 
}
