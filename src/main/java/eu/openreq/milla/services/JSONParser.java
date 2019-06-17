package eu.openreq.milla.services;
import java.util.List;

import com.google.gson.Gson;

import eu.openreq.milla.models.json.*;

public class JSONParser {
	static Gson gson = new Gson();
	public static Project project;
	public static Requirement requirement;
	public static List<Project> projects;
	public static List<Requirement> requirements;
	public static List<Requirement> dependent_requirements;
	public static List<Dependency> dependencies;
	public static InputExtractor input;
	
	public static void parseToOpenReqObjects(String jsonString) throws com.google.gson.JsonSyntaxException {
		input = gson.fromJson(jsonString, InputExtractor.class);
		project = input.getProject();
		projects = input.getProjects();
		requirement = input.getRequirement();
		requirements = input.getRequirements();
		dependencies = input.getDependencies();
		dependent_requirements = input.getDependentRequirements();
		if(dependencies!= null) {
			fixDependencyIds(dependencies);
		}
	}
	
	//Dependencies that come from UPC do not have IDs, so it is necessary to give them IDs before trying to save to the database
	private static void fixDependencyIds(List<Dependency> dependencies) {
		for(Dependency dependency : dependencies) {
			if(dependency.getId()==null) {
				dependency.setId(dependency.getFromid() + "_" + dependency.getToid() + "_" + dependency.getDependency_type());
			}
		}
	}
	
}
