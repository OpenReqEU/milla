package eu.openreq.milla.services;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
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
	
	public String getListOfProjects() {
		try {
			return rt.getForObject(mallikasAddress + "/listOfProjects", String.class);
		}
		catch (HttpClientErrorException e) {
			System.out.println("Error " + e);
			e.printStackTrace();
			return e.getResponseBodyAsString();
		}
	}
	
	/**
	 * Send request to Mallikas to get all Requirements in the database
	 * @return
	 */
	public String getAllRequirements() {
		try {
			return rt.getForObject(mallikasAddress + "/allRequirements", String.class);
		}
		catch (HttpClientErrorException e) {
			System.out.println("Error " + e);
			e.printStackTrace();
			return e.getResponseBodyAsString();
		}
	}
		
	/**
	 * Send request to Mallikas to get a List of Requirements and their Dependencies as a String (based on a List of selected Requirement IDs) 
	 * @param ids Collection<String> containing selected Requirement IDs
	 * @return
	 */
	public String getSelectedRequirements(Collection<String> ids) {
		try {
			return rt.postForObject(mallikasAddress + "/selectedReqs", ids, String.class);
		}
		catch (HttpClientErrorException e) { 
			System.out.println("Error " + e);
			e.printStackTrace();
			return e.getResponseBodyAsString();
		}
	}
	
	/**
	 * Send request to Mallikas to get a String (List of Requirements that are in the same Project and their Dependencies)
	 * @param projectId Id of the Project
	 * @param includeProposed indicates if also proposed dependencies are included
	 * @return String containing all requirements and their dependencies in the same project
	 */
	public String getAllRequirementsInProject(String projectId, boolean includeProposed, boolean requirementsOnly) {	
		try {
			return rt.getForObject(mallikasAddress + "/projectRequirements?projectId=" + projectId + 
					"&includeProposed=" + includeProposed + "&requirementsOnly=" + requirementsOnly, String.class);	
		} catch (HttpClientErrorException e) {
			System.out.println("Error " + e);
			e.printStackTrace();
			return e.getResponseBodyAsString();
		}
	}
	/**
	 * Send a request to Mallikas contained within a RequestParams object
	 * @param params
	 * @param objectType
	 * @return
	 */
	public String requestWithParams(RequestParams params, String objectType) {	
		try {
			return rt.postForObject(mallikasAddress + "/" + objectType + "ByParams", params, String.class);		
		} catch (HttpClientErrorException e) {
			System.out.println("Error " + e);
			e.printStackTrace();
			return e.getResponseBodyAsString();
		}
	}
	
	/**
	 * Post the project to Mallikas
	 * @param project
	 * @return
	 */
	public String postProject(Project project) {
		try {
			return rt.postForObject(mallikasAddress + "/importProject", project, String.class);

		} catch (HttpClientErrorException e) {
			System.out.println("Error " + e);
			e.printStackTrace();
			return e.getResponseBodyAsString();
		}
	}
	
	/**
	 * Post the requirements to be updated in Mallikas
	 * @param requirements
	 * @param projectId
	 * @return
	 */
	public String updateRequirements(Collection<Requirement> requirements, String projectId) {
		try {
			return rt.postForObject(mallikasAddress + "/updateRequirements?projectId=" + projectId, requirements, String.class);	
		} catch (HttpClientErrorException e) {
			System.out.println("Error " + e);
			e.printStackTrace();
			return e.getResponseBodyAsString();
		}
			
	}
	
	/**
	 * Post the dependencies to be updated in Mallikas, along with some params used in saving
	 * @param dependencies
	 * @param proposed
	 * @param userInput
	 * @return
	 */
	public String updateDependencies(Collection<Dependency> dependencies, Boolean proposed, Boolean userInput) {
		
		String completeAddress = mallikasAddress + "/updateDependencies";
		
		if (proposed) {
			FileService fs = new FileService();
			fs.logDependencies(dependencies);
			completeAddress += "?isProposed=true";
		}
		if (userInput) {
			completeAddress += "?userInput=true";
		}

		try {
			rt.postForObject(completeAddress, dependencies, String.class);
			return "Update successful!";

		} catch (HttpClientErrorException e) {
			System.out.println("Error " + e);
			e.printStackTrace();
			return e.getResponseBodyAsString();
		}
	}
	
	public String convertAndUpdateDependencies(String dependencies, Boolean proposed, Boolean userInput) {
		Collection<Dependency> convertedDependencies = parseStringToDependencies(dependencies);
		return updateDependencies(convertedDependencies, proposed, userInput);
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
		
		try {	
			return rt.postForObject(mallikasAddress + "/updateProjectSpecifiedRequirements?projectId=" + projectId, updatedReqs, String.class);
		} catch (HttpClientErrorException e) {
			System.out.println("Error " + e);
			e.printStackTrace();
			return e.getResponseBodyAsString();
		}
		
	}

	/**
	 * Parse JSON String to Dependencies
	 * @param dependencies
	 * @return
	 */
	public Collection<Dependency> parseStringToDependencies(String dependencies) {
		Gson gson = new Gson();
		JsonParser parser = new JsonParser();
		JsonElement dependencyElement = parser.parse(dependencies);
		JsonArray dependenciesJSON = dependencyElement.getAsJsonArray();
		Type listType = new TypeToken<List<Dependency>>(){}.getType();
		
		Collection<Dependency> updatedDependencies = gson.fromJson(dependenciesJSON, listType);
		
		return updatedDependencies;
	}
	
	/**
	 * Parse JSON String to Requirements
	 * @param requirements
	 * @return
	 */
//	private Collection<Requirement> parseStringToRequirements(String requirements) {
//		Gson gson = new Gson();
//		JsonParser parser = new JsonParser();
//		JsonElement reqElement = parser.parse(requirements);
//		JsonArray requirementsJSON = reqElement.getAsJsonArray();
//		Type listType = new TypeToken<List<Dependency>>(){}.getType();
//		
//		Collection<Requirement> updatedRequirements = gson.fromJson(requirementsJSON, listType);
//		
//		return updatedRequirements;
//	}


}
