package eu.openreq.milla.services;
import java.util.List;

import com.google.gson.Gson;

import eu.openreq.milla.models.json.*;

public class OpenReqJSONParser {
	static Gson gson = new Gson();
	private Project project;
	private Requirement requirement;
	private List<Project> projects;
	private List<Requirement> requirements;
	private List<Requirement> dependent_requirements;
	private List<Dependency> dependencies;
	
	public OpenReqJSONParser (String jsonString) throws com.google.gson.JsonSyntaxException {
		InputExtractor input = gson.fromJson(jsonString, InputExtractor.class);
		this.project = input.getProject();
		this.projects = input.getProjects();
		this.requirement = input.getRequirement();
		this.requirements = input.getRequirements();
		this.dependencies = input.getDependencies();
		this.dependent_requirements = input.getDependentRequirements();
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

	public Project getProject() {
		return project;
	}

	public Requirement getRequirement() {
		return requirement;
	}

	public List<Project> getProjects() {
		return projects;
	}

	public List<Requirement> getRequirements() {
		return requirements;
	}

	public List<Requirement> getDependent_requirements() {
		return dependent_requirements;
	}

	public List<Dependency> getDependencies() {
		return dependencies;
	}

	
}
