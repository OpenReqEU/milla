package eu.openreq.milla.services;

import java.util.Collection;
import java.util.List;

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
import eu.openreq.milla.models.json.RequestParams;
import eu.openreq.milla.models.json.Requirement;

@Service
public class MallikasService {
	
	
	/**
	 * Send request to Mallikas to get all Requirements in the database
	 * @param url the address in Mallikas
	 * @return
	 */
	public String getAllRequirementsFromMallikas(String url) {
		
		RestTemplate rt = new RestTemplate();	
		String reqsAndDependencies = null; 
		
		try {
			reqsAndDependencies=rt.getForObject(url, String.class);
		}
		catch (HttpClientErrorException e) {
			e.printStackTrace();
		}
		
		return reqsAndDependencies;
	}
	
	/**
	 * Send request to Mallikas to get one Requirement with a selected id
	 * @param url the address in Mallikas
	 * @param id String identifier of the Requirement
	 * @return
	 */
	public String getOneRequirementFromMallikas(String url, String id) {
		RestTemplate rt = new RestTemplate();	
		String req = null;
		
		try {
			req = rt.postForObject(url, id, String.class);	
		}
		catch (HttpClientErrorException e) {
			System.out.println("Error " + e);
			e.printStackTrace();
		}
		return req;
	}
		
	/**
	 * Send request to Mallikas to get a List of Requirements and their Dependecies as a String (based on a List of selected Requirement IDs) 
	 * @param ids List<String> containing selected Requirement IDs
	 * @param url the address in Mallikas
	 * @return
	 */
	public String getSelectedRequirementsFromMallikas(Collection<String> ids, String url) {
		
		RestTemplate rt = new RestTemplate();
		String reqs = null;
		
		try {
			reqs= rt.postForObject(url, ids, String.class);
		}
		catch (HttpClientErrorException e) { 
			System.out.println("Error " + e);
			e.printStackTrace();
		}
		
		return reqs;
	}
	
	/**
	 * Send request to Mallikas to get a List of Requirements and their Dependecies as a String (based on a List of selected Requirement IDs) 
	 * @param ids List<String> containing selected Requirement IDs
	 * @param url the address in Mallikas
	 * @return
	 */
	public String getRequirementsSinceDateFromMallikas(Long date, String url) {
		
		RestTemplate rt = new RestTemplate();
		String reqs = null;
		
		try {
			reqs= rt.postForObject(url, date, String.class);
		}
		catch (HttpClientErrorException e) { 
			System.out.println("Error " + e);
			e.printStackTrace();
		}
		
		return reqs;
	}
	
	/**
	 * Send request to Mallikas to get a String (List of Requirements that are in the same Project and their Dependencies)
	 * @param projectId Id of the Project
	 * @param url the address in Mallikas
	 * @return String containing all requirements and their dependencies in the same project
	 */
	public String getAllRequirementsInProjectFromMallikas(String projectId, String url) {
		RestTemplate rt = new RestTemplate();	
		String reqs = null;
		try {
			reqs = rt.postForObject(url, projectId, String.class);		
		} catch (HttpClientErrorException e) {
			System.out.println("Error " + e);
			e.printStackTrace();
		}
		return reqs;
	}
		
	/**
	 * Post the searched type and status to Mallikas
	 * @param type
	 * @param status
	 * @param url
	 * @return
	 */
	public String getAllRequirementsWithTypeAndStatusFromMallikas(String type, String status, String url) {

		RestTemplate rt = new RestTemplate();	
		String reqs = null;
		
		String whole = createTypeStatusString(type, status);
		
		try {	
			reqs = rt.postForObject(url, whole, String.class);
			
		} catch (HttpClientErrorException e) {
			System.out.println("Error " + e);
			e.printStackTrace();
		}
		return reqs;
	}

	/**
	 * Can be used to search requirements with a certain Resolution or Dependency type
	 * @param searched
	 * @param url
	 * @return
	 */
	public String getAllRequirementsWithSearchedStringFromMallikas(String searched, String url) {
		RestTemplate rt = new RestTemplate();	
		String reqs = null;
		try {	
			reqs = rt.postForObject(url, searched, String.class);
			
		} catch (HttpClientErrorException e) {
			System.out.println("Error " + e);
			e.printStackTrace();
		}
		return reqs;
	}
	
	/**
	 * Checks if the user wishes to exclude either type or status from the search (must write "No type" or "No status" to the requested input fields) 
	 * @param type
	 * @param status
	 * @return
	 */
	private String createTypeStatusString(String type, String status) {
		String type2 = type;
		String status2 = status;
		if(type.equalsIgnoreCase("No type")) {
			type2="null";
		}
		if(status.equalsIgnoreCase("No status")) {
			status2="null";
		}
		return type2+"+"+status2;
	}
	
	/**
	 * Send updated dependencies as a String to Mallikas
	 * @param dependencies
	 * @param url
	 * @return
	 */
	public String updateSelectedDependencies(String dependencies, String url, Boolean proposed) {
		RestTemplate rt = new RestTemplate();	
		Collection<Dependency> updatedDependencies = parseStringToDependencies(dependencies);
		
		if (proposed) {
			FileService fs = new FileService();
			fs.logDependencies(updatedDependencies);
		}

		try {
			String response = rt.postForObject(url, updatedDependencies, String.class);
			return "Update successful! " + response;

		} catch (HttpClientErrorException e) {
			return "Mallikas error:\n\n" + e.getResponseBodyAsString() + " "+ e.getStatusCode();
		}
	}
	
//	/**
//	 * Send updated dependencies as a String to Mallikas
//	 * @param dependencies
//	 * @param url
//	 * @return
//	 */
//	public String updateUPCDependencies(Collection<Dependency> dependencies, String url) {
//		RestTemplate rt = new RestTemplate();	
//
//		String response = null;
//
//		try {
//			response = rt.postForObject(url, dependencies, String.class);
//			return response;
//
//		} catch (HttpClientErrorException e) {
//			return "Mallikas error:\n\n" + e.getResponseBodyAsString() + " "+ e.getStatusCode();
//		}
//	}
	
	/**
	 * Send updated requirements as a String to Mallikas
	 * @param requirements
	 * @param url
	 * @return
	 */
	public String updateSelectedRequirements(String requirements, String url) {
		RestTemplate rt = new RestTemplate();	
		Collection<Requirement> updatedRequirements = parseStringToRequirements(requirements);

		String response = null;

		try {
			response = rt.postForObject(url, updatedRequirements, String.class);
			return response;

		} catch (HttpClientErrorException e) {
			e.printStackTrace();
			return "Mallikas error:\n\n" + e.getResponseBodyAsString() + " " + e.getStatusCode();
		}
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

	/**
	 * Send a request to Mallikas contained within a RequestParams object
	 * @param params
	 * @param url
	 * @return
	 */
	public String sendRequestWithParamsToMallikas(RequestParams params, String url) {
		RestTemplate rt = new RestTemplate();	
		String reqs = null;
		try {
			reqs = rt.postForObject(url, params, String.class);		
		} catch (HttpClientErrorException e) {
			System.out.println("Error " + e);
			e.printStackTrace();
		}
		return reqs;
	}

}
