package eu.openreq.milla.controllers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
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
import eu.openreq.milla.services.UpdateService;
import eu.openreq.milla.qtjiraimporter.ProjectIssues;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@SpringBootApplication
@RestController
@RequestMapping(value = "/")
public class MillaController {

	@Value("${milla.mulperiAddress}")
	private String mulperiAddress;

	@Value("${milla.mallikasAddress}")
	private String mallikasAddress;

	@Autowired
	FormatTransformerService transformer;

	@Autowired
	MallikasService mallikasService;
	
	@Autowired
	UpdateService updateService;

	/**
	 * Post Requirements and Dependencies to Mulperi.
	 * 
	 * @param data
	 * @param path
	 * @return
	 * @throws IOException
	 */
//	@ApiOperation(value = "OBSOLETE: Relay POST to Mulperi", 
//			notes = "OBSOLETE: Post a model or configuration request to Mulperi")
//	//@ResponseBody
//	@PostMapping(value = "data")
	private ResponseEntity<?> postToMulperi(@RequestBody String data) throws IOException {

		RestTemplate rt = new RestTemplate();

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		String actualPath = "models/requirementsToChoco";

		String completeAddress = mulperiAddress + actualPath;

		HttpEntity<String> entity = new HttpEntity<String>(data, headers);
		ResponseEntity<?> response = null;
		try {
			response = rt.postForEntity(completeAddress, entity, String.class);
		} catch (HttpClientErrorException e) {
			return new ResponseEntity<>("Mulperi error:\n\n" + e.getResponseBodyAsString(), e.getStatusCode());
		}

		return response;
	}

	/**
	 * Fetch Requirements by params given from Mallikas, and send
	 * to Mulperi
	 * 
	 * @param
	 * @return ResponseEntity<?>
	 * @throws IOException
	 */
	@ApiOperation(value = "Construct a transitive closure by sending all requirements of the selected project to Mulperi", 
	notes = "<b>Functionality</b>: All requirements in the same project are fetched from Mallikas database and send to Mulperi to construct a transitive closure."
			+ "<br><b>Precondition</b>: The project, including its requirements, has been cached in Mallikas.<br>"
			+ "<b>Postcondition</b>: Mulperi has a transitive closure of each requirement for a project up to depth five.<br>"
			+ "<br><b>Prarameter: </b>"
			+ "<br>projectId: The project id in Mallikas (e.g., QTWB).")

	//@ResponseBody
	@PostMapping(value = "sendProjectToMulperiWithParams")
	public ResponseEntity<?> sendProjectToMulperiWithParams(@RequestParam String projectId, @RequestParam boolean includeProposedDependencies) throws IOException {

		String completeAddress = mallikasAddress + "requirementsByParams";

		RequestParams params = new RequestParams();
		
		params.setProjectId(projectId);
		params.setIncludeProposed(includeProposedDependencies);
		
		String reqsInProject = mallikasService.sendRequestWithParamsToMallikas(params, completeAddress);

		if (reqsInProject == null) {
			return new ResponseEntity<>("Requirements not found \n\n", HttpStatus.NOT_FOUND);
		}
		return this.postToMulperi(reqsInProject);
	}
	
	/**
	 * Fetch Requirements that are in the selected Project from Mallikas, and send
	 * to Mulperi
	 * 
	 * @param
	 * @return ResponseEntity<?>
	 * @throws IOException
	 */
	@ApiOperation(value = "Construct a transitive closure by sending all requirements of the selected project to Mulperi", 
	notes = "<b>Functionality</b>: All requirements in the same project are fetched from Mallikas database and send to Mulperi to construct a transitive closure."
			+ "<br><b>Precondition</b>: The project, including its requirements, has been cached in Mallikas.<br>"
			+ "<b>Postcondition</b>: Mulperi has a transitive closure of each requirement for a project up to depth five.<br>"
			+ "<br><b>Prarameter: </b>"
			+ "<br>projectId: The project id in Mallikas (e.g., QTWB).")

	//@ResponseBody
	@PostMapping(value = "sendProjectToMulperi")
	public ResponseEntity<?> sendProjectToMulperi(@RequestParam String projectId) throws IOException {

		String completeAddress = mallikasAddress + "projectRequirements";

		
		String reqsInProject = mallikasService.getAllRequirementsInProjectFromMallikas(projectId, completeAddress);

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
//	@ApiOperation(value = "Store (TBD or update?) requirements to mallikas.", 
//	notes = "<b>Functionality</b>: Add or update an array of requirements in OpenReq JSON format in Mallikas database. "
//			+ "<br><b>Postcondition</b>: Requirements are stored in Mallikas."
//			+ "<br><b>Note: </b> The project needs to be updated separately to contain references to the new requirements."
//			+ "<br><b>Prarameter: </b>"
//			+ "<br>requirements: An array of requirements in OpenReq JSON.")
//	//@ResponseBody
//	@PostMapping(value = "requirements")
	private ResponseEntity<?> postRequirementsToMallikas(@RequestBody Collection<Requirement> requirements)
			throws IOException {

		RestTemplate rt = new RestTemplate();

		String completeAddress = mallikasAddress + "updateRequirements";

		Collection<Requirement> issueList = requirements;
		ResponseEntity<?> response = null;

		try {
			response = rt.postForEntity(completeAddress, issueList, Collection.class);

		} catch (HttpClientErrorException e) {
			return new ResponseEntity<>("Mallikas error:\n\n" + e.getResponseBodyAsString(), e.getStatusCode());
		}
		return response;
	}

	/**
	 * Post a Collection of OpenReq JSON Dependencies to Mallikas database
	 * 
	 * @param dependencies
	 *            Collection<Dependency> received as a parameter, requirements to be
	 *            saved into the database
	 * @return ResponseEntity<?>
	 * @throws IOException
	 */
//	@ApiOperation(value = "Store (TBD or update?) dependencies to Mallikas", 
//			notes = "<br><b>Functionality</b>: Add or update a set of dependencies as an array of dependencies in OpenReq JSON format. The dependencies are for the existing requirements in the Mallikas database."
//					+ "<br><b>Postcondition</b>: The dependencies are added or updated to the existing requirements in Mallikas. If a dependency exist, it is updated."
//					+ "<br><b>Prarameter: </b>"
//					+ "<br>dependencies: An array of dependencies in OpenReq JSON format. ")
//	//@ResponseBody
//	@PostMapping(value = "dependencies")
	public ResponseEntity<?> postDependenciesToMallikas(@RequestBody Collection<Dependency> dependencies)
			throws IOException {

		RestTemplate rt = new RestTemplate();

		String completeAddress = mallikasAddress + "updateDependencies";

		Collection<Dependency> dependencyList = dependencies;
		ResponseEntity<?> response = null;

		try {
			response = rt.postForEntity(completeAddress, dependencyList, Collection.class);

		} catch (HttpClientErrorException e) {
			return new ResponseEntity<>("Mallikas error:\n\n" + e.getResponseBodyAsString(), e.getStatusCode());
		}
		return response;
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
//			+ "<br><b>Prarameter: </b>"
//			+ "<br>project: A project in OpenReq JSON.")
//	//@ResponseBody
//	@PostMapping(value = "project")
	private ResponseEntity<?> postProjectToMallikas(@RequestBody Project project) throws IOException {

		RestTemplate rt = new RestTemplate();

		String completeAddress = mallikasAddress + "project";

		ResponseEntity<?> response = null;

		try {
			response = rt.postForEntity(completeAddress, project, Project.class);

		} catch (HttpClientErrorException e) {
			return new ResponseEntity<>("Mallikas error:\n\n" + e.getResponseBodyAsString(), e.getStatusCode());
		}
		return response;
	}

	// Might be unnecessary to have this and postUpdatedRequirementsToMallikas, but
	// keeping them for now, might be useful with UPC
	/**
	 * Post a Collection of updated (or new) OpenReq JSON Dependencies to Mallikas
	 * database
	 * 
	 * @param dependencies
	 *            Collection<Dependency> received as a parameter, dependencies to be
	 *            updated
	 * @return ResponseEntity<?>
	 * @throws IOException
	 */
	@ApiOperation(value = "OBSOLETE? Post updated dependencies to Mallikas", notes = "Post updated dependencies as a String list to Mallikas database")
	//@ResponseBody
	@PostMapping(value = "updateDependencies")
	public ResponseEntity<?> postUpdatedDependenciesToMallikas(@RequestBody String dependencies) throws IOException {

		String completeAddress = mallikasAddress + "updateDependencies";

		String updated = null;

		try {
			updated = mallikasService.updateSelectedDependencies(dependencies, completeAddress, false);

		} catch (HttpClientErrorException e) {
			return new ResponseEntity<>("Mallikas error:\n\n" + e.getResponseBodyAsString(), e.getStatusCode());
		}
		ResponseEntity<String> response = new ResponseEntity<>(updated, HttpStatus.OK);
		return response;
	}

	@ApiOperation(value = "OBSOLETE? Post updated requirements to Mallikas", notes = "Post updated requirements as a String list to Mallikas database")
	//@ResponseBody
	@PostMapping(value = "updateRequirements")
	public ResponseEntity<?> postUpdatedRequirementsToMallikas(@RequestBody String requirements) throws IOException {

		String completeAddress = mallikasAddress + "updateRequirements";

		String updated = null;

		try {
			updated = mallikasService.updateSelectedRequirements(requirements, completeAddress);

		} catch (HttpClientErrorException e) {
			return new ResponseEntity<>("Mallikas error:\n\n" + e.getResponseBodyAsString(), e.getStatusCode());
		}

		ResponseEntity<String> response = new ResponseEntity<>(updated, HttpStatus.OK);
		return response;
	}

//	/**
//	 * Fetch Requirements related to the selected Component (OpenReq Classifier)
//	 * 
//	 * @param
//	 * @return ResponseEntity<?>
//	 * @throws IOException
//	 */
//	@ApiOperation(value = "Fetch requirements in the same component", notes = "Fetch all requirements in the same component from Mallikas database")
//	@ResponseBody
//	@PostMapping(value = "requirementsInComponent")
//	public ResponseEntity<?> getRequirementsInSameComponent(@RequestBody String componentId) throws IOException {
//
//		String completeAddress = mallikasAddress + "classifiers";
//
//		String reqsInComponent = mallikasService.getAllRequirementsWithClassifierFromMallikas(componentId,
//				completeAddress);
//
//		if (reqsInComponent == null) {
//			return new ResponseEntity<>("Requirements not found \n\n", HttpStatus.NOT_FOUND);
//		}
//		ResponseEntity<String> response = new ResponseEntity<>(reqsInComponent, HttpStatus.FOUND);
//		return response;
//	}

	/**
	 * Fetch Requirements that are in the selected Project
	 * 
	 * @param
	 * @return ResponseEntity<?>
	 * @throws IOException
	 */
	@ApiOperation(value = "Fetch all requirements  of a project in OpenReq JSON format.", 
			notes = "<b>Functionality</b>: Fetch all requirements of a project including their dependencies that are cached in Mallikas database in the OpenReq JSON format."
					+ "<br><b>Precondition</b>: The project has been cached in Mallikas."
					+ "<br><b>Postcondition</b>: An OpenReq JSON of the project including requirements and dependencies is produced."
					+ "<br><b>Prarameter: </b>"
					+ "<br>projectId: The project id in Mallikas (e.g., QTWB).")
	//@ResponseBody
	@PostMapping(value = "requirementsInProject")
	public ResponseEntity<?> getRequirementsInProject(@RequestBody String projectId) throws IOException {
		String completeAddress = mallikasAddress + "projectRequirements";

		String reqsInProject = mallikasService.getAllRequirementsInProjectFromMallikas(projectId, completeAddress);

		if (reqsInProject == null || reqsInProject.equals("")) {
			return new ResponseEntity<>("Requirements not found \n\n", HttpStatus.NOT_FOUND);
		}
		ResponseEntity<String> response = new ResponseEntity<>(reqsInProject, HttpStatus.FOUND);
		return response;
	}

	/**
	 * Fetch a List of selected Requirements from Mallikas
	 * 
	 * @param project
	 *            Project received as a parameter
	 * @return ResponseEntity<?>
	 * @throws IOException
	 */
	@ApiOperation(value = "Fetch a specified set of requirements in OpenReq JSON format.", 
	notes = "<b>Functionality</b>: Fetch a specied requirements including their dependencies that are cached in Mallikas database in the OpenReq JSON format."
			+ "<br><b>Precondition</b>: The project has been cached in Mallikas."
			+ "<br><b>Postcondition</b>: An OpenReq JSON of the requirements and their dependencies is produced."
			+ "<br><b>Prarameter: </b>"
			+ "<br>ids: ids as a String array, e.g. [\"QTWB-1\", \"QTWB-2\"] ")
	//@ResponseBody
	@PostMapping(value = "requirementsWithIds")
	public ResponseEntity<?> getRequirementsWithIds(@RequestBody Collection<String> ids) throws IOException {
		String completeAddress = mallikasAddress + "selectedReqs";

		String reqsWithIds = mallikasService.getSelectedRequirementsFromMallikas(ids, completeAddress);

		if (reqsWithIds == null || reqsWithIds.equals("")) {
			return new ResponseEntity<>("Requirements not found \n\n", HttpStatus.NOT_FOUND);
		}
		ResponseEntity<String> response = new ResponseEntity<>(reqsWithIds, HttpStatus.FOUND);
		return response;
	}

	/**
	 * Fetch a List of Requirements created after the Date given from Mallikas
	 * 
	 * @param project
	 *            Project received as a parameter
	 * @return ResponseEntity<?>
	 * @throws IOException
	 */
	@ApiOperation(value = "Fetch all requirements since a given date in OpenReq JSON format.", 
	notes = "<b>Functionality</b>: Fetch requirements, including their dependencies that are cached in Mallikas database in the OpenReq JSON format from any project."
			+ "<br><b>Precondition</b>: The project has been cached in Mallikas."
			+ "<br><b>Postcondition</b>: An OpenReq JSON of the requirements and their dependencies is produced."
			+ "<br><b>Parameter: </b>"
			+ "<br>date: Date in proper format such as \"2019-03-05T11:13:39.529Z\"")
	//@ResponseBody
	@PostMapping(value = "requirementsSinceDate")
	public ResponseEntity<?> getRequirementsSinceDate(@RequestBody Date date) throws IOException {
		String completeAddress = mallikasAddress + "reqsSinceDate";

		String reqsWithDate = mallikasService.getRequirementsSinceDateFromMallikas(date.getTime(), completeAddress);

		if (reqsWithDate == null || reqsWithDate.equals("")) {
			return new ResponseEntity<>("Requirements not found \n\n", HttpStatus.NOT_FOUND);
		}
		ResponseEntity<String> response = new ResponseEntity<>(reqsWithDate, HttpStatus.FOUND);
		return response;
	}
	
	/**
	 * Fetch a list of requirements according to their type and/or status from the
	 * database.
	 * 
	 * @param type
	 * @return
	 * @throws IOException
	 */
	@ApiOperation(value = "Fetch requirements that have the selected requirement type and status in OpenReq JSON format.", 
	notes = "<b>Functionality</b>: Fetch requirements that have the selected requirement type and status that are cached in Mallikas database in the OpenReq JSON format."
			+ "<br><b>Precondition</b>: The project has been cached in Mallikas."
			+ "<br><b>Postcondition</b>: An OpenReq JSON of the requirements and their dependencies is produced."
			+ "<br><b>Parameter: </b>"
			+ "<br>type: Requirement type in all caps, e.g. BUG"
			+ "<br>status: Requirement status in all caps, e.g. NEW")
	//@ResponseBody
	@PostMapping(value = "requirementsWithTypeAndStatus")
	public ResponseEntity<?> getRequirementsWithTypeAndStatus(@RequestParam String type, @RequestParam String status)
			throws IOException {
		String completeAddress = mallikasAddress + "reqsWithType";

		String reqsWithType = mallikasService.getAllRequirementsWithTypeAndStatusFromMallikas(type, status,
				completeAddress);

		if (reqsWithType == null || reqsWithType.equals("")) {
			return new ResponseEntity<>("Search failed, requirements not found \n\n", HttpStatus.NOT_FOUND);
		}
		ResponseEntity<String> response = new ResponseEntity<>(reqsWithType, HttpStatus.FOUND);
		return response;
	}

	/**
	 * Fetch a list of requirements according to their resolution value from the
	 * database.
	 * 
	 * @param resolution
	 * @return
	 * @throws IOException
	 */
	@ApiOperation(value = "Fetch requirements that have the selected resolution in OpenReq JSON format.", 
			notes = "<b>Functionality</b>: Fetch requirements that have the selected resolution cached in Mallikas database in the OpenReq JSON format."
					+ "<br><b>Precondition</b>: The project has been cached in Mallikas."
					+ "<br><b>Postcondition</b>: An OpenReq JSON of the requirements and their dependencies is produced."
					+ "<br><b>Parameter: </b>"
					+ "<br>resolution: Resolution can be e.g. Duplicate, Unresolved etc. ")
	//@ResponseBody
	@PostMapping(value = "requirementsWithResolution")
	public ResponseEntity<?> getRequirementsWithResolution(@RequestParam String resolution) throws IOException {
		String completeAddress = mallikasAddress + "reqsWithResolution";

		String reqsWithResolution = mallikasService.getAllRequirementsWithSearchedStringFromMallikas(resolution,
				completeAddress);

		if (reqsWithResolution == null || reqsWithResolution.equals("")) {
			return new ResponseEntity<>("Search failed, requirements not found \n\n", HttpStatus.NOT_FOUND);
		}
		ResponseEntity<String> response = new ResponseEntity<>(reqsWithResolution, HttpStatus.FOUND);
		return response;
	}
	
	/**
	 * Fetch a list of requirements according to their dependency type from the
	 * database.
	 * 
	 * @param type
	 * @return
	 * @throws IOException
	 */
	@ApiOperation(value = "Fetch requirements that have the selected dependency type in OpenReq JSON format.", 
			notes = "<b>Functionality</b>: Fetch requirements that have the selected dependency type and cached in Mallikas database in the OpenReq JSON format."
					+ "<br><b>Precondition</b>: The project has been cached in Mallikas."
					+ "<br><b>Postcondition</b>: An OpenReq JSON of the requirements and their dependencies is produced."
					+ "<br><b>Parameter: </b>"
					+ "<br>type: Dependency type can be e.g. DUPLICATES, REQUIRES etc.")
	//@ResponseBody
	@PostMapping(value = "requirementsWithDependencyType")
	public ResponseEntity<?> getRequirementsWithDependencyType(@RequestBody String type) throws IOException {
		String completeAddress = mallikasAddress + "reqsWithDependencyType";
		
		String modifiedType = type.toUpperCase();

		String reqsWithDependencyType = mallikasService.getAllRequirementsWithSearchedStringFromMallikas(modifiedType,
				completeAddress);

		if (reqsWithDependencyType == null || reqsWithDependencyType.equals("")) {
			return new ResponseEntity<>("Search failed, requirements not found \n\n", HttpStatus.NOT_FOUND);
		}
		ResponseEntity<String> response = new ResponseEntity<>(reqsWithDependencyType, HttpStatus.FOUND);
		return response;
	}
	
	/**
	 * Fetch a list of requirements from the database based on a list of various parameters.
	 * 
	 * @param type
	 * @return
	 * @throws IOException
	 */
	@ApiOperation(value = "Fetch requirements based on various parameters given in JSON format.", 
			notes = "<b>Functionality</b>: Fetch requirements based on various parameters given in JSON format."
					+ "<br><b>Precondition</b>: The project has been cached in Mallikas."
					+ "<br><b>Postcondition</b>: An OpenReq JSON of the requirements and their dependencies is produced."
					+ "<br><b>Parameter: </b>"
					+ "<br>requestParams: Object containing various parameters")
	//@ResponseBody
	@PostMapping(value = "requirementsByParams")
	public ResponseEntity<?> getRequirementsByVariousParams(@RequestBody RequestParams params) throws IOException {
		String completeAddress = mallikasAddress + "requirementsByParams";

		String reqsWithDependencyType = mallikasService.sendRequestWithParamsToMallikas(params,
				completeAddress);

		if (reqsWithDependencyType == null || reqsWithDependencyType.equals("")) {
			return new ResponseEntity<>("Search failed, requirements not found \n\n", HttpStatus.NOT_FOUND);
		}
		ResponseEntity<String> response = new ResponseEntity<>(reqsWithDependencyType, HttpStatus.FOUND);
		return response;
	}

	/**
	 * Fetch one Requirement and Requirements that depend on it from Mallikas
	 * 
	 * @return
	 * @throws IOException
	 */
	@ApiOperation(value = "Fetch one selected requirement and requirements dependent on it from the database", 
			notes = "<b>Functionality</b>: Fetch the specified requirement and its dependent requirements including the dependency objects from Mallikas in OpenReq JSON format. "
					+ "<br><b>Precondition</b>: The project has been cached in Mallikas."
					+ "<br><b>Postcondition</b>: An OpenReq JSON of the requested requirement and an array of dependent requirements "
					+ "and dependencies are produced."
					+ "<br><b>Parameter: </b>"
					+ "<br>id: The id of the requirement, e.g. QTWB-30.")
	//@ResponseBody
	@PostMapping(value = "requirementAndDependents")
	public ResponseEntity<?> getOneRequirementAndDependents(@RequestBody String id) throws IOException {

		String completeAddress = mallikasAddress + "dependents";
		String requirement = mallikasService.getOneRequirementFromMallikas(completeAddress, id);

		if (requirement == null || requirement.equals("")) {
			return new ResponseEntity<>("Requirements not found \n\n", HttpStatus.NOT_FOUND);
		}
		ResponseEntity<String> response = new ResponseEntity<>(requirement, HttpStatus.FOUND);
		return response;
	}

	/**
	 * Uses QtJiraImporter to get the issues of a selected project in OpenReq JSON
	 * format and sends them to Mallikas database.
	 * 
	 * @param projectId,
	 *            ID of the selected project
	 * @return ResponseEntity if successful
	 * @throws IOException
	 */
	@ApiOperation(value = "Import a selected project from Qt Jira and store a cache of the project to Mallikas", 
			notes = "<b>Functionality</b>: This is the full data import from Qt Jira. "
					+ "A selected project including its issues are fetched from Qt Jira. "
					+ "The data is converted to OpenReq JSON requirements and dependencies, and OpenReq JSON project is constructed."
					+ "The project including its requirements and dependencies is cached to Mallikas database"
					+ "<br><b>Postcondition</b>: The selected project is cached in Mallikas to be managed in the OpenReq infrastructure. "
					+ "The same project name (id) is used in Qt Jira and Mallikas."
					+ "<br><b>Note:</b> For update rather than full import, see \"qtjiraUpdated\", which is  more effiecient for large projects.</b>"
					+ "<br><b>Parameter: </b>"
					+ "<br>projectId: The project id in Qt Jira, which is then used also in Mallikas (e.g., QTWB).", 
					response = String.class)
	@ApiResponses(value = { @ApiResponse(code = 201, message = "Success, all requirements and dependencies downloaded"),
			@ApiResponse(code = 400, message = "Failure, ex. malformed JSON"),
			@ApiResponse(code = 500, message = "Failure, ex. invalid URLs") })
	//@ResponseBody
	@PostMapping(value = "qtjira")
	public ResponseEntity<?> importFromQtJira(@RequestBody String projectId) throws IOException {

		ProjectIssues projectIssues = new ProjectIssues(projectId);
		
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
		
		transformer.readFixVersionsToHashMap(projectId);
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
				this.postDependenciesToMallikas(dependencies);
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

			ResponseEntity<String> response = new ResponseEntity<>("All requirements and dependencies downloaded",
					HttpStatus.OK);
			return response;
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
	 * @return
	 * @throws IOException
	 */
	@ApiOperation(value = "Fetch all requirements including their dependencies from the Mallikas database in OpenReq JSON format.", 
			notes = "<b>Functionality</b>: Fetch all requirements from Mallikas database in OpenReq JSON requirement and dependency format. "
					+ "Project object is not included."
					+ "This is practically an export of the entire database and, thus, dataset. "
					+ "In Swagger, the data might get too large to be shown in the response field."
					+ "For large database, Milla and Mallikas java VM need extra memory."
			+ "<br><b>Precondition</b>: There is at least one project and requirement cached in Mallikas.<br>"
			+ "<b>Postcondition</b>: Requirements and dependencies in OpenReq JSON is produced.<br>")
	//@ResponseBody
	@RequestMapping(value = "allRequirements", method = RequestMethod.GET)
	public ResponseEntity<?> getAllRequirements() throws IOException {

		System.out.println("getAllRequirements called");
		String completeAddress = mallikasAddress + "allRequirements";

		String allRequirements = mallikasService.getAllRequirementsFromMallikas(completeAddress);

		if (allRequirements == null || allRequirements.equals("")) {
			return new ResponseEntity<>("Requirements not found \n\n", HttpStatus.NOT_FOUND);
		}
		ResponseEntity<String> response = new ResponseEntity<>(allRequirements, HttpStatus.FOUND);
		return response;
	}
	
	
	/**
	 * Uses QtJiraImporter to get the latest updated issues of a selected project in OpenReq JSON
	 * format and sends them to Mallikas database.
	 * 
	 * @param projectId,
	 *            ID of the selected project
	 * @return ResponseEntity if successful
	 * @throws IOException
	 */
	@ApiOperation(value = "Import the updated issues from Qt Jira to Mallikas.", 
			notes = "<b>Functionality</b>: Import the updated Jira issues of a project from Qt Jira and send the issues as OpenReq JSON requirements and dependencies to be cached in Mallikas database"
			+ "<br><b>Precondition</b>: The project has been cached in Mallikas.<br>"
			+ "<b>Postcondition</b>: New or changed Jira issues are updated to Mallikas<br>"
			+ "<b>Exception</b>: This is not full synchronization."
			+ "<br><b>Prarameter: </b>"
			+ "<br>projectId: The project id that is used in Qt Jira and Mallikas (e.g., QTWB).", 
			response = String.class)

	@ApiResponses(value = { @ApiResponse(code = 200, message = "Success, all updated requirements and dependencies downloaded"),
			@ApiResponse(code = 400, message = "Failure, ex. malformed JSON"),
			@ApiResponse(code = 500, message = "Failure, ex. invalid URLs") })
	//@ResponseBody
	@PostMapping(value = "qtjiraUpdated")
	public ResponseEntity<?> importUpdatedFromQtJira(@RequestBody String projectId) throws IOException {
		
		Person person = new Person();
		person.setUsername("user_" + projectId);
		person.setEmail("dummyEmail");
		
		try {
			return updateService.getAllUpdatedIssues(projectId, person);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new ResponseEntity<>("Download failed", HttpStatus.BAD_REQUEST);
	}
	 
}
