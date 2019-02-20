package eu.openreq.milla.controllers;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import eu.openreq.milla.models.json.Project;
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
	@ApiOperation(value = "Fetch only the most recent issues of a project from Qt Jira to Mallikas and update the graph in KeljuCaas", notes = "Post recent issues in a project to Mallikas database and KeljuCaas")
	//@ResponseBody
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
	
	@ApiOperation(value = "Get the transitive closure of a requirement",
			notes = "Returns the transitive closure of a given requirement to the depth of 5",
			response = String.class)
	@ApiResponses(value = { 
			@ApiResponse(code = 200, message = "Success, returns JSON model"),
			@ApiResponse(code = 400, message = "Failure, ex. model not found"), 
			@ApiResponse(code = 409, message = "Conflict")}) 
	@RequestMapping(value = "/getTransitiveClosureOfARequirement", method = RequestMethod.POST)
	public ResponseEntity<?> getTransitiveClosureOfRequirement(@RequestBody String requirementId) throws IOException {
		RestTemplate rt = new RestTemplate();

		String completeAddress = mulperiAddress + "models/findTransitiveClosureOfRequirement";
		
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

}
