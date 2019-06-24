package eu.openreq.milla.services;
import java.util.List;

import com.google.gson.Gson;

import eu.openreq.milla.models.json.*;

public class OpenReqJSONParser {
	static Gson gson = new Gson();
	public Project project;
	public Requirement requirement;
	public List<Project> projects;
	public List<Requirement> requirements;
	public List<Requirement> dependent_requirements;
	public List<Dependency> dependencies;
	public InputExtractor input;
	
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

	public void setProject(Project project) {
		this.project = project;
	}

	public Requirement getRequirement() {
		return requirement;
	}

	public void setRequirement(Requirement requirement) {
		this.requirement = requirement;
	}

	public List<Project> getProjects() {
		return projects;
	}

	public void setProjects(List<Project> projects) {
		this.projects = projects;
	}

	public List<Requirement> getRequirements() {
		return requirements;
	}

	public void setRequirements(List<Requirement> requirements) {
		this.requirements = requirements;
	}

	public List<Requirement> getDependent_requirements() {
		return dependent_requirements;
	}

	public void setDependent_requirements(List<Requirement> dependent_requirements) {
		this.dependent_requirements = dependent_requirements;
	}

	public List<Dependency> getDependencies() {
		return dependencies;
	}

	public void setDependencies(List<Dependency> dependencies) {
		this.dependencies = dependencies;
	}
	
}
