package eu.openreq.milla.services;
import java.util.List;

import com.google.gson.Gson;

import eu.openreq.milla.models.json.*;

public class OpenReqJSONParser {
	static final Gson gson = new Gson();
	private final Project project;
	private final Requirement requirement;
	private final List<Project> projects;
	private final List<Requirement> requirements;
	private final List<Requirement> dependent_requirements;
	private final List<Dependency> dependencies;

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
