package eu.openreq.milla.services;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;

import eu.openreq.milla.models.json.Dependency;
import eu.openreq.milla.models.json.Project;
import eu.openreq.milla.models.json.RequestParams;
import eu.openreq.milla.models.json.Requirement;

@Service
public class MallikasService {
	
	@Value("${milla.mallikasAddress}")
	private String mallikasAddress;
	
	@Autowired
	RestTemplate rt;
	
	@Autowired
	FileService fs;
	
	Gson gson = new Gson();
	
	public String getListOfProjects() {
		return getFromMallikas("/listOfProjects");
	}
	
	/**
	 * Send request to Mallikas to get all Requirements in the database
	 * @return
	 */
	public String getAllRequirements() {
		return getFromMallikas("/allRequirements");
	}
		
	/**
	 * Send request to Mallikas to get a List of Requirements and their Dependencies as a String (based on a List of selected Requirement IDs) 
	 * @param ids Collection<String> containing selected Requirement IDs
	 * @return
	 */
	public String getSelectedRequirements(Collection<String> ids) {
		return postObjectToMallikas(ids, "/selectedReqs");
	}
	
	/**
	 * Send request to Mallikas to get a String (List of Requirements that are in the same Project and their Dependencies)
	 * @param projectId Id of the Project
	 * @param includeProposed indicates if also proposed dependencies are included
	 * @return String containing all requirements and their dependencies in the same project
	 */
	public String getAllRequirementsInProject(String projectId, boolean includeProposed, boolean requirementsOnly) {	
		return getFromMallikas("/projectRequirements?projectId=" + projectId + 
				"&includeProposed=" + includeProposed + "&requirementsOnly=" + requirementsOnly);	

	}
	
	/**
	 * Send a request to Mallikas contained within a RequestParams object
	 * @param params
	 * @param objectType
	 * @return
	 */
	public String requestWithParams(RequestParams params, String objectType) {	
		return postObjectToMallikas(params, "/" + objectType + "ByParams");		
	}
	
	/**
	 * Post the project to Mallikas
	 * @param project
	 * @return
	 */
	public String postProject(Project project) {
		return postObjectToMallikas(project, "/importProject");		
	}
	
	/**
	 * Post the requirements to be updated in Mallikas
	 * @param requirements
	 * @param projectId
	 * @return
	 */
	public String updateRequirements(Collection<Requirement> requirements, String projectId) {
		return postObjectToMallikas(requirements, "/updateRequirements?projectId=" + projectId);			
	}
	
	/**
	 * Post the dependencies to be updated in Mallikas, along with some params used in saving
	 * @param dependencies
	 * @param proposed
	 * @param userInput
	 * @return
	 */
	public ResponseEntity<String> updateDependencies(Collection<Dependency> dependencies, Boolean proposed, Boolean userInput) {
		
		String completeAddress = mallikasAddress + "/updateDependencies";
		
		if (proposed) {
			fs.logDependencies(dependencies);
			completeAddress += "?isProposed=true";
		}
		if (userInput) {
			fs.logDependencies(dependencies);
			completeAddress += "?userInput=true";
		}

		try {
			return rt.postForEntity(completeAddress, dependencies, String.class);
		} catch (HttpClientErrorException e) {
			return new ResponseEntity<String>(errorResponse(e), HttpStatus.BAD_REQUEST);
		}
	}
	
	/**
	 * Converts a string to dependencies before sending it for update
	 * @param dependencies
	 * @param proposed
	 * @param userInput
	 * @return
	 */
	public ResponseEntity<String> convertAndUpdateDependencies(String dependencies, Boolean proposed, Boolean userInput) {
		try {
			Type type = new TypeToken<List<Dependency>>(){}.getType();
			List<Dependency> convertedDependencies = new Gson().fromJson(dependencies, type);
			return updateDependencies(convertedDependencies, proposed, userInput);
		} catch (Exception e) {
			return new ResponseEntity<String>(e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}
	
	/**
	 * Post the ids of the updated requirements to Mallikas so that the Project object can be updated. 
	 * @param reqIds
	 * @param projectId
	 * @return
	 */
	public String updateReqIds(Collection<String> reqIds, String projectId) {
		Map<String, Collection<String>> updatedReqs = new HashMap<String, Collection<String>>();
		updatedReqs.put(projectId, reqIds);	
		return postObjectToMallikas(updatedReqs, "/updateProjectSpecifiedRequirements?projectId=" + projectId);
	}
	
	/**
	 * Returns the dependencies with their from/to IDs corrected, would they have been the wrong way around
	 * @param dependencies
	 * @return
	 */
	public List<Dependency> correctIdsForDependencies(List<Dependency> dependencies) {
		String response = postObjectToMallikas(dependencies, "/correctIdsForDependencies");
		Type depListType = new TypeToken<List<Dependency>>(){}.getType();
		return gson.fromJson(response, depListType);
	}

	/**
	 * Sends the dependencies to Mallikas, gets a HasAmap where the dependencies are labeled under their respective projects
	 * @param dependencies
	 * @return
	 */
	public HashMap<String, List<Dependency>> projectsForDependencies(List<Dependency> dependencies) {
		String response = postObjectToMallikas(dependencies, "/projectsForDependencies");
		Type mapType = new TypeToken<HashMap<String, List<Dependency>>>() {}.getType();
		return gson.fromJson(response, mapType);
	}
	
	/**
	 * Get object from Mallikas (as a string)
	 * @param urlTail
	 * @return
	 */
	private String getFromMallikas(String urlTail) {
		try {
			return rt.getForObject(mallikasAddress + urlTail, String.class);
		} catch (HttpClientErrorException e) {
			return errorResponse(e);
		}
	}
	
	/**
	 * Posts the object to Mallikas
	 * @param object
	 * @param urlTail
	 * @return
	 */
	private String postObjectToMallikas(Object object, String urlTail) {
		try {
			return rt.postForObject(mallikasAddress + urlTail, object, String.class);
		} catch (HttpClientErrorException e) {
			return errorResponse(e);
		}
	}
	
	private String errorResponse(HttpClientErrorException e) {
		System.out.println("Error " + e);
		e.printStackTrace();
		return e.getResponseBodyAsString();
	}


}
