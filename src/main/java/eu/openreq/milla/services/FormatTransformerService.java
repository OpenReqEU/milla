package eu.openreq.milla.services;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.springframework.stereotype.Service;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

import eu.openreq.milla.models.jira.Comments;
import eu.openreq.milla.models.jira.Issue;
import eu.openreq.milla.models.jira.Component;
import eu.openreq.milla.models.jira.Issuelink;
import eu.openreq.milla.models.jira.Subtask;
import eu.openreq.milla.models.json.Classifier;
import eu.openreq.milla.models.json.Comment;
import eu.openreq.milla.models.json.Dependency;
import eu.openreq.milla.models.json.Dependency_type;
import eu.openreq.milla.models.json.Project;
import eu.openreq.milla.models.json.Requirement;
import eu.openreq.milla.models.json.Requirement_status;
import eu.openreq.milla.models.json.Requirement_type;

/**
 * Methods used to convert between formats
 * 
 * @author iivorait
 * @author tlaurinen
 * @author ekettu
 */
@Service
public class FormatTransformerService {

	private List<Dependency> dependencies;

	private List<String> requirementIds;

	/**
	 * Converts JsonElements to Jira Issues
	 * 
	 * @param jsonElements
	 *            a collection of JsonElement objects
	 * @return a List of Issue objects
	 * @throws IOException
	 */
	public List<Issue> convertJsonElementsToIssues(List<JsonElement> jsonElements, String projectId)
			throws IOException {

		long start = System.nanoTime();

		Gson gson = new Gson();
		List<Issue> issues = new ArrayList<>();

		for (int i = 0; i < jsonElements.size(); i++) {
			JsonElement element = jsonElements.get(i);
			Issue issue = gson.fromJson(element, Issue.class);
			issues.add(issue);
			element = null;
			issue = null;
		}
		long end = System.nanoTime();
		long durationSec = (end - start) / 1000000000;
		double durationMin = durationSec / 60.0;
		System.out.println("Lists done, it took " + durationSec + " second(s) or " + durationMin + " minute(s).");

		return issues;
	}

	/**
	 * Converts a List of Jira Issues into OpenReq Json Requirements, and creates a
	 * List of Requirement Ids (as Strings) that will be given to a Project.
	 * Requirements do not know their Project, but the Project knows the Ids of its
	 * Requirements
	 * 
	 * @param issues
	 *            List of Jira Issues
	 * @return a collection of Requirement objects
	 */
	public Collection<Requirement> convertIssuesToJson(List<Issue> issues, String projectId) throws Exception {
		dependencies = new ArrayList<>();
		HashMap<String, Requirement> requirements = new HashMap<>();

		requirementIds = new ArrayList<>();

		for (Issue issue : issues) {
			try {
				Requirement req = new Requirement();
				req.setId(issue.getKey()); // Murmeli doesn't mind hyphens, hopefully? 
				String name = fixSpecialCharacters(issue.getFields().getSummary());
				req.setName(name);
				requirements.put(req.getId(), req);
				requirementIds.add(req.getId());

				int priority = Integer.parseInt(issue.getFields().getPriority().getId()); // Note! This might not be
																							// actually a good idea, QT
																							// priorities not numerical,
																							// might still work
				req.setPriority(priority);

				setStatusForReq(req, issue.getFields().getStatus().getName().toLowerCase());

				req.setCreated_at(new Date().getTime());

				addCommentsToReq(issue, req);
				addDependencies(issue, req);
				addClassifiers(issue, req);

				updateParentEpic(requirements, issue, req);

				List<Subtask> subtasks = issue.getFields().getSubtasks();
				if (subtasks != null && !subtasks.isEmpty()) {
					for (Subtask subtask : subtasks) {
						addSubtask(requirements, req, subtask);
					}
				}
			} catch (Exception e) {
				//System.out.println("Error in requirement creation: " + e);
				 e.printStackTrace();
			}
		}

		return requirements.values();
	}

	/**
	 * Method for removing special characters from a String, a quick and dirty
	 * version to avoid UTF-8 errors.
	 * 
	 * @param name
	 *            that needs to have special characters removed
	 * @return a fixed version of the name
	 */
	private String fixSpecialCharacters(String name) {
		String fixedName = name.replaceAll("[^\\x20-\\x7e]", ""); // TODO This is a quick fix, must be modified into a
																	// better version
		return fixedName;
	}

	/**
	 * Assigns status to a Requirement according to the status (String) received
	 * from a Jira Issue
	 * 
	 * @param req
	 *            Requirement needing a status
	 * @param status
	 *            String value received from a Jira Issue
	 */
	private void setStatusForReq(Requirement req, String status) {
		switch (status) {
		case "reported":
			req.setStatus(Requirement_status.SUBMITTED);
		case "need more info":
			req.setStatus(Requirement_status.DEFERRED);
		case "open":
			req.setStatus(Requirement_status.PENDING);
		case "in progress":
			req.setStatus(Requirement_status.NEW); // ?
		case "withdrawn":
			req.setStatus(Requirement_status.REJECTED);
		case "implemented":
			req.setStatus(Requirement_status.DRAFT);
		case "verified":
			req.setStatus(Requirement_status.ACCEPTED);
		case "closed":
			req.setStatus(Requirement_status.COMPLETED);
		}
	}

	/**
	 * Creates a list of Comments for a Requirement based on the Comments of a Jira
	 * Issue
	 * 
	 * @param issue
	 *            Issue that has Comments
	 * @param req
	 *            Requirement receiving the Comment
	 */
	private void addCommentsToReq(Issue issue, Requirement req) {
		if (!issue.getFields().getComment().getComments().isEmpty()) {
			for (Comments comment : issue.getFields().getComment().getComments()) {
				Comment jsonComment = new Comment();
				jsonComment.setId(comment.getId());
				jsonComment.setText(comment.getBody());

				String date = String.valueOf(comment.getCreated());
				long created = setCreatedDate(date);
				jsonComment.setCreated_at(created);
				req.getComments().add(jsonComment);
			}
		}
	}

	/**
	 * Method for parsing date data that is received as a String from a Jira Issue
	 * 
	 * @param created
	 *            date and time data as a String
	 * @return the date and time as a Long milliseconds
	 */
	private long setCreatedDate(String created) {
		String created2 = splitString(created);
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");

		long milliseconds;
		try {
			Date date = format.parse(created2);
			milliseconds = date.getTime();
		} catch (ParseException e) {
			System.out.println("Error in parsing the date: " + e);
			return -1;
		}
		return milliseconds;
	}

	/**
	 * Helper method for splitting a date and time String from the right place
	 * 
	 * @param word
	 *            String to be split
	 * @return the first part of the split String
	 */
	private String splitString(String word) {
		String[] parts = word.split("\\+");
		String part = parts[0];
		return part;
	}

	/**
	 * Creates OpenReq Json Dependency objects based on Jira IssueLinks and Json
	 * Requirements. Only Outward Issues are considered, since the IssueLinks go
	 * both ways.
	 * 
	 * @param issue
	 *            Issue that has IssueLinks
	 * @param req
	 */
	private void addDependencies(Issue issue, Requirement req) {
		if (issue.getFields().getIssuelinks() != null) {
			for (Issuelink link : issue.getFields().getIssuelinks()) {
				// if (!"depends on".equals(link.getType().getOutward())) { //Is this necessary?
				// continue;
				// }
				if (link.getOutwardIssue() == null || link.getOutwardIssue().getKey() == null) {
					continue;
				}
				Dependency dependency = new Dependency();
				dependency.setFromId(req.getId());
				dependency.setToId(link.getOutwardIssue().getKey());
				setDependencyType(dependency, link.getType().getName());
				dependency.setId(req.getId()+"_"+link.getOutwardIssue().getKey()+"_"+dependency.getDependency_type());
				dependencies.add(dependency);
			}
		}
	}

	/**
	 * Assigns Dependency_type to a Dependency according to the type (String)
	 * received from a Jira IssueLink
	 * 
	 * @param dependency
	 *            Dependency needing a Dependency_type
	 * @param type
	 *            String received from an IssueLink
	 */
	private void setDependencyType(Dependency dependency, String type) {
		String type2 = type.toLowerCase();
		switch (type2) {
		case "dependency":
			dependency.setDependency_type(Dependency_type.REQUIRES);
		case "relates":
			dependency.setDependency_type(Dependency_type.SIMILAR);
		case "duplicate":
			dependency.setDependency_type(Dependency_type.DUPLICATES);
		case "replacement":
			dependency.setDependency_type(Dependency_type.REPLACES);
		case "work breakdown":
			dependency.setDependency_type(Dependency_type.REFINES);
		case "test":
			dependency.setDependency_type(Dependency_type.REFINES);
			;
		}
	}

	/**
	 * Adds children ("subRequirements") to a Requirement, Subtasks are received
	 * from an Issue, and converted into Requirements.
	 * 
	 * @param requirements
	 * @param req
	 * @param subtask
	 */
	private void addSubtask(HashMap<String, Requirement> requirements, Requirement req, Subtask subtask) {
		Requirement subReq = new Requirement();
		subReq.setId(subtask.getKey());
		subReq.setName(subtask.getFields().getSummary());
		subReq.setCreated_at(new Date().getTime());

		int priority = Integer.parseInt(subtask.getFields().getPriority().getId()); // Note! This might not be actually
																					// a good idea, QT priorities not
																					// numerical, might still work
		subReq.setPriority(priority);

		setStatusForReq(subReq, subtask.getFields().getStatus().getName().toLowerCase());

		requirements.put(subReq.getId(), subReq); /// Do this or not?
		req.getChildren().add(subReq);
	}

	/**
	 * Jira Issues know their parents (Epics)
	 * (issue.getFields().getCustomfield10400()), but Requirements know their
	 * children. This method adds the knowledge of a child Requirement to a parent
	 * Requirement, and creates a new Epic Requirement, if necessary.
	 * 
	 * @param requirements
	 * @param issue
	 * @param req
	 */
	private void updateParentEpic(HashMap<String, Requirement> requirements, Issue issue, Requirement req) {
		Object epicKeyObject = issue.getFields().getCustomfield10400();
		if (epicKeyObject == null) {
			return; // No parent
		}
		String epicKey = epicKeyObject.toString();

		Requirement epic = requirements.get(epicKey);

		if (epic == null) { // Parent not yet created
			epic = new Requirement();
			epic.setId(epicKey);
			epic.setName("Epic " + epicKey); // Is this okay?
			setRequirementType(epic, "epic");
			epic.setCreated_at(new Date().getTime());
			addCommentsToReq(issue, epic);
			addClassifiers(issue, epic);
			requirements.put(epicKey, epic);
			// Should we add Commments and Classifiers etc also? Or not have this method at
			// all? Or check in creating Requirements if there are already Requirements of
			// that id?
		}

		epic.getChildren().add(req);
	}

	/**
	 * Assigns a Requirement_type to a Requirement, type received as a String from a
	 * Jira Issue
	 * 
	 * @param req
	 *            Requirement needing a Requirement_type
	 * @param type
	 *            String received from an Issue
	 */
	private void setRequirementType(Requirement req, String type) {
		String type2 = type.toLowerCase();
		switch (type2) {
		case "epic":
			req.setRequirement_type(Requirement_type.EPIC);
		case "user story":
			req.setRequirement_type(Requirement_type.USER_STORY);
		case "bug":
			req.setRequirement_type(Requirement_type.BUG);
		case "sub-task":
			req.setRequirement_type(Requirement_type.TASK);
		}
	}

	/**
	 * Assigns a Classifier (Component) to a Requirement based on the Component of
	 * an Issue
	 * 
	 * @param issue
	 *            Issue that has (a) Component(s)
	 * @param req
	 *            Requirement that needs Classifiers
	 */
	private void addClassifiers(Issue issue, Requirement req) {
		if (!issue.getFields().getComponents().isEmpty()) {
			for (Component component : issue.getFields().getComponents()) {
				Classifier classifier = new Classifier();
				classifier.setId(component.getId());
				classifier.setName(component.getName());
				classifier.setCreated_at(new Date().getTime());
				req.getClassifierResults().add(classifier);
			}
		}
	}

	/**
	 * Creates an OpenReq Json Project object
	 * 
	 * @param projectId
	 *            identifier of a Qt Jira project
	 * @param requirementIds
	 *            List of Requirement ids
	 * @return a new Project
	 */
	public Project createProject(String projectId, List<String> reqIds) {
		Project project = new Project();
		project.setId(projectId);
		project.setName(projectId);
		project.setCreated_at(new Date().getTime());
		project.setSpecifiedRequirements(reqIds);
		return project;
	}
	
	public List<Dependency> getDependencies() {
		return dependencies;
	}
	
	public List<String> getRequirementIds() {
		return requirementIds;
	}
	

	
	// Below old Mulson methods

	// /**
	// * Converts a List of Issue objects into Mulson Requirements
	// *
	// * @param issues
	// * List of Issue objects
	// * @return a collection of Requirement objects
	// */
	// public Collection<Requirement> convertIssuesToMulson(List<Issue> issues,
	// String projectId) throws Exception {
	// HashMap<String, Requirement> requirements = new HashMap<>();
	// for (Issue issue : issues) {
	// try {
	// Requirement req = new Requirement();
	// req.setRequirementId(issue.getKey().replace("-", "_")); // Kumbang doesn't
	// like hyphens
	// String name = fixSpecialCharacters(issue.getFields().getSummary());
	// req.setName(name);
	// requirements.put(req.getRequirementId(), req);
	//
	// addAttribute(req, "priority", issue.getFields().getPriority().getId());
	// addAttribute(req, "status", issue.getFields().getStatus().getName());
	//
	// addRequiredRelationships(issue, req);
	// updateParentEpic(requirements, issue, req);
	//
	// List<Subtask> subtasks = issue.getFields().getSubtasks();
	// if (subtasks != null && !subtasks.isEmpty()) {
	// for (Subtask subtask : subtasks) {
	// addSubtask(requirements, req, subtask);
	// }
	// }
	// } catch (Exception e) {
	// System.out.println("Error " + e);
	// // e.printStackTrace();
	// }
	// }
	// return requirements.values();
	// }

	// private void addAttribute(Requirement req, String name, String value) {
	// try {
	// Attribute priority = new Attribute();
	// priority.setName(name);
	// ArrayList<String> priorities = new ArrayList<>();
	// priorities.add(value.replace(" ", "_")); // Kumbang doesn't like spaces,
	// either
	// priority.setValues(priorities);
	// // req.getAttributes().add(priority);
	// } catch (Exception e) {
	// System.out.println("No " + name);
	// }
	// }

	// private void addSubtask(HashMap<String, Requirement> requirements,
	// Requirement req, Subtask subtask) {
	// Requirement req2 = new Requirement();
	// req2.setRequirementId(subtask.getKey().replace("-", "_"));
	// req2.setName(subtask.getFields().getSummary());
	// requirements.put(req2.getRequirementId(), req2);
	//
	// addAttribute(req2, "priority", subtask.getFields().getPriority().getId());
	// addAttribute(req2, "status", subtask.getFields().getStatus().getName());
	//
	// SubFeature subfeat = new SubFeature();
	// ArrayList<String> types = new ArrayList<>();
	// types.add(req2.getRequirementId());
	// subfeat.setTypes(types);
	// subfeat.setRole(req2.getRequirementId());
	// subfeat.setCardinality("0-1");
	//
	// req.getSubfeatures().add(subfeat);
	//
	// // No issue links (requirements)?
	// }

	// private void updateParentEpic(HashMap<String, Requirement> requirements,
	// Issue issue, Requirement req) {
	// Object epicKeyObject = issue.getFields().getCustomfield10400();
	// if (epicKeyObject == null) {
	// return; // No parent
	// }
	// String epicKey = epicKeyObject.toString().replace("-", "_");
	//
	// Requirement epic = requirements.get(epicKey);
	//
	// if (epic == null) { // Parent not yet created
	// epic = new Requirement();
	// epic.setRequirementId(epicKey);
	// epic.setName("Epic " + epicKey);
	// requirements.put(epicKey, epic);
	// }
	//
	// SubFeature subfeat = new SubFeature();
	// ArrayList<String> types = new ArrayList<>();
	// types.add(req.getRequirementId());
	// subfeat.setTypes(types);
	// subfeat.setRole(req.getRequirementId());
	// subfeat.setCardinality("0-1");
	//
	// epic.getSubfeatures().add(subfeat);
	// }
	//
	// private void addRequiredRelationships(Issue issue, Requirement req) {
	// if (issue.getFields().getIssuelinks() != null) {
	// for (Issuelink link : issue.getFields().getIssuelinks()) {
	// if (!"depends on".equals(link.getType().getOutward())) {
	// continue;
	// }
	// if (link.getOutwardIssue() == null || link.getOutwardIssue().getKey() ==
	// null) {
	// continue;
	// }
	// Relationship rel = new Relationship();
	// req.getRelationships().add(rel);
	// rel.setTargetId(link.getOutwardIssue().getKey().replace("-", "_"));
	// rel.setType("requires");
	// }
	// }
	// }
	//
	// private void addAttribute(Requirement req, String name, String value) {
	// try {
	// Attribute priority = new Attribute();
	// priority.setName(name);
	// ArrayList<String> priorities = new ArrayList<>();
	// priorities.add(value.replace(" ", "_")); // Kumbang doesn't like spaces,
	// either
	// priority.setValues(priorities);
	// req.getAttributes().add(priority);
	// } catch (Exception e) {
	// System.out.println("No " + name);
	// }
	// }

	// /**
	// * Method for creating a "mock" Issue of Issues that are mentioned in a
	// * project's Issues' issueLinks, but do not belong to the same project.
	// *
	// * @param key
	// * of the Issue associated with the project's issues, a real
	// * key/identifier
	// * @param i
	// * an index used for creating mock identifiers
	// * @return Issue with made-up fields etc
	// */
	// private Issue createMockIssue(String key, int i) {
	// Issue otherIssue = new Issue();
	// otherIssue.setKey(key);
	// otherIssue.setExpand("");
	// Fields fields = new Fields();
	// Priority priority = new Priority();
	// priority.setId("" + i);
	// Status__ status = new Status__();
	// status.setName("mockstatus");
	// fields.setSummary("A mock issue");
	// fields.setPriority(priority);
	// fields.setStatus(status);
	// fields.setSubtasks(null);
	// otherIssue.setFields(fields);
	// otherIssue.setId("" + i + 1);
	// otherIssue.setSelf("");
	//
	// return otherIssue;
	// }
	//
	// /**
	// * Creates an IssueObject
	// *
	// * @param issue
	// * @param element
	// */
	// private IssueObject createNewIssueObject(Issue issue, String element) {
	// IssueObject issueObject = new IssueObject();
	// issueObject.setKey(issue.getKey());
	// issueObject.setIssueId(issue.getId());
	// issueObject.setContent(element);
	// issueObject.setUpdated(issue.getFields().getUpdated());
	//// issueObject.setTimestamp(LocalDateTime.now());
	// return issueObject;
	// }
	//
	// public List<IssueObject> getIssueObjects() {
	// return issueObjects;
	// }
}
