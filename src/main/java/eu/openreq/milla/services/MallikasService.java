package eu.openreq.milla.services;

import java.util.Collection;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
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
	
	/**
	 * Send request to Mallikas to get all Requirements in the database
	 * @return
	 */
	public String getAllRequirements() {
	
		try {
			return rt.getForObject(mallikasAddress + "/allRequirements", String.class);
		}
		catch (HttpClientErrorException e) {
			e.printStackTrace();
			return e.getResponseBodyAsString();
		}
	}
		
	/**
	 * Send request to Mallikas to get a List of Requirements and their Dependencies as a String (based on a List of selected Requirement IDs) 
	 * @param ids List<String> containing selected Requirement IDs
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
	 * @return String containing all requirements and their dependencies in the same project
	 */
	public String getAllRequirementsInProject(String projectId) {	
		try {
			return rt.getForObject(mallikasAddress + "/projectRequirements?projectId=" + projectId, String.class);	
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
	
	public String postProject(Project project) {
		try {
			return rt.postForEntity(mallikasAddress + "/importProject", project, String.class).getBody();

		} catch (HttpClientErrorException e) {
			return e.getResponseBodyAsString();
		}
	}
	
	public String updateRequirements(Collection<Requirement> requirements) {
		try {
			return rt.postForObject(mallikasAddress + "/updateRequirements", requirements, String.class);	
		} catch (HttpClientErrorException e) {
			System.out.println("Error " + e);
			e.printStackTrace();
			return e.getMessage();
		}
			
	}
	
	/**
	 * Send updated dependencies as a String to Mallikas
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
			return "Mallikas error:\n\n" + e.getResponseBodyAsString() + " "+ e.getStatusCode();
		}
	}
	
	public String convertAndUpdateDependencies(String dependencies, Boolean proposed, Boolean userInput) {
		Collection<Dependency> convertedDependencies = parseStringToDependencies(dependencies);
		return updateDependencies(convertedDependencies, proposed, userInput);
	}
	

	/**
	 * Parse JSON String to Dependencies
	 * @param dependencies
	 * @return
	 */
	private Collection<Dependency> parseStringToDependencies(String dependencies) {
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
	private Collection<Requirement> parseStringToRequirements(String requirements) {
		Gson gson = new Gson();
		JsonParser parser = new JsonParser();
		JsonElement reqElement = parser.parse(requirements);
		JsonArray requirementsJSON = reqElement.getAsJsonArray();
		Type listType = new TypeToken<List<Dependency>>(){}.getType();
		
		Collection<Requirement> updatedRequirements = gson.fromJson(requirementsJSON, listType);
		
		return updatedRequirements;
	}


}
