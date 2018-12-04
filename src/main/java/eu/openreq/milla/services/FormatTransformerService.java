package eu.openreq.milla.services;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import eu.openreq.milla.models.jira.Comments;
import eu.openreq.milla.models.jira.Component;
import eu.openreq.milla.models.jira.Issue;
import eu.openreq.milla.models.jira.Issuelink;
import eu.openreq.milla.models.jira.Platform;
import eu.openreq.milla.models.jira.Platforms;
import eu.openreq.milla.models.jira.Subtask;
import eu.openreq.milla.models.jira.Version;
import eu.openreq.milla.models.jira.FixVersion;
import eu.openreq.milla.models.json.Comment;
import eu.openreq.milla.models.json.Dependency;
import eu.openreq.milla.models.json.Dependency_status;
import eu.openreq.milla.models.json.Dependency_type;
import eu.openreq.milla.models.json.Person;
import eu.openreq.milla.models.json.Project;
import eu.openreq.milla.models.json.Requirement;
import eu.openreq.milla.models.json.RequirementPart;
import eu.openreq.milla.models.json.Requirement_status;
import eu.openreq.milla.models.json.Requirement_type;

/**
 * Methods used to convert between formats (JsonElements to Jira Issues, and
 * Issues to OpenReq Requirements)
 * 
 * @author iivorait
 * @author tlaurinen
 * @author ekettu
 */
@Service
public class FormatTransformerService {

	/**
	 * List of Dependencies between Requirements
	 */
	private List<Dependency> dependencies;

	/**
	 * List of Requirement IDs that are related to a Project
	 */
	private List<String> requirementIds;

	private FileService fileService;

	private Map<String, Integer> fixVersions;

	private int epicCount;

	public int getEpicCount() {
		return epicCount;
	}

	private int subtaskCount;

	public int getSubtaskCount() {
		return subtaskCount;
	}

	public void readFixVersionsToHashMap(String projectId) {
		fileService = new FileService();
		fixVersions = fileService.readFixVersionsFromFile(projectId);
	}

	/**
	 * Converts JsonElements to Jira Issues
	 * 
	 * @param jsonElements
	 *            a collection of JsonElement objects
	 * @return a List of Issue objects
	 * @throws IOException
	 */
	public List<Issue> convertJsonElementsToIssues(Collection<JsonElement> jsonElements) throws IOException {

		long start = System.nanoTime();

		Gson gson = new Gson();
		List<Issue> issues = new ArrayList<>();

		List<JsonElement> elements = new ArrayList<>(jsonElements);
		for (int i = 0; i < jsonElements.size(); i++) {
			JsonElement element = elements.get(i);

			JsonObject issueJSON = element.getAsJsonObject();
			Issue issue = gson.fromJson(issueJSON, Issue.class);
			issue.getFields().setCustomfield10400(issueJSON.getAsJsonObject("fields").get("customfield_10400")); 
			
			if(issueJSON.getAsJsonObject("fields").get("customfield_11100")!=null && !issueJSON.getAsJsonObject("fields").get("customfield_11100").isJsonNull() ) {
			JsonArray array = gson.fromJson(issueJSON.getAsJsonObject("fields").get("customfield_11100"), JsonArray.class);
			List<Platform> plats = new ArrayList<Platform>();
			for(JsonElement elem : array) {
				JsonObject platJSON = elem.getAsJsonObject();
				Platform platform = gson.fromJson(platJSON, Platform.class);
				plats.add(platform);
			}
			Platform[] plats2 = new Platform[plats.size()];
			for(int j = 0; j < plats.size(); j++) {
				plats2[j] = plats.get(j);
			}
			Platforms platforms = new Platforms();
			platforms.setplatforms(plats2);
			issue.getFields().setCustomfield11100(platforms);
			}
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
	public Collection<Requirement> convertIssuesToJson(Collection<Issue> issues, String projectId, Person person)
			throws Exception {
		dependencies = new ArrayList<>();
		HashMap<String, Requirement> requirements = new HashMap<>();

		epicCount = 0;
		subtaskCount = 0;

		requirementIds = new ArrayList<>();

		for (Issue issue : issues) {
			try {
				Requirement req = new Requirement();
				req.setId(issue.getKey()); // Murmeli doesn't mind hyphens, hopefully?
				String name = fixSpecialCharacters(issue.getFields().getSummary());
				req.setName(name);
				String text = fixSpecialCharacters(issue.getFields().getDescription());
				if (text != null && !text.equals("")) {
					req.setText(text);
				}
				requirements.put(req.getId(), req);
				requirementIds.add(req.getId());
				int priority = Integer.parseInt(issue.getFields().getPriority().getId());

				setRightPriority(req, priority);

				setStatusForReq(req, issue.getFields().getStatus().getName());
				setRequirementType(req, issue.getFields().getIssuetype().getName());

				req.setCreated_at(setCreatedDate(issue.getFields().getCreated()));
				req.setModified_at(setCreatedDate(issue.getFields().getUpdated()));

				addCommentsToReq(issue, req, person);
				addDependencies(issue, req);

				addResolutionToRequirementParts(issue, req);
				addEnvironmentToRequirementParts(issue, req);
				addLabelsToRequirementParts(issue, req);
				addVersionsToRequirementParts(issue, req);
				addPlatformsToRequirementParts(issue, req);
				addFixVersionsToRequirementParts(issue, req);
				addComponentsToRequirementParts(issue, req);
				updateParentEpic(requirements, issue, req);

				List<Subtask> subtasks = issue.getFields().getSubtasks();
				if (subtasks != null && !subtasks.isEmpty()) {
					for (Subtask subtask : subtasks) {
						addSubtask(req, subtask);
					}
				}
			} catch (Exception e) {
				// System.out.println("Error in requirement creation: " + e);
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
	private String fixSpecialCharacters(String name) { // TODO this might not be necessary anymore but is left here for
														// demo safety
		String fixedName = name;
		if (name != null && !name.equals("")) {
			fixedName = name.replaceAll("[^\\x20-\\x7e]", ""); // TODO This is a quick fix, must be modified into a
		} // better version
		return fixedName;
	}

	/**
	 * Qt priorities with ids 7 and 6 are in a "wrong" order, hence this fix
	 * 
	 * @param req
	 * @param priority
	 */
	private void setRightPriority(Requirement req, int priority) {
		if (priority == 6) {
			req.setPriority(7);
		} else if (priority == 7) {
			req.setPriority(6);
		} else {
			req.setPriority(priority);
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
	private void addCommentsToReq(Issue issue, Requirement req, Person person) {
		if (issue.getFields().getComment() != null && !issue.getFields().getComment().getComments().isEmpty()) {
			for (Comments comment : issue.getFields().getComment().getComments()) {
				Comment jsonComment = new Comment();
				jsonComment.setId(comment.getId());
				jsonComment.setText(comment.getBody());
				jsonComment.setCommentDoneBy(person);
				// System.out.println(jsonComment.getCommentDoneBy().getUsername());
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
		if (issue.getFields().getIssuelinks() != null && !issue.getFields().getIssuelinks().isEmpty()) {
			for (Issuelink link : issue.getFields().getIssuelinks()) {
				if (link.getOutwardIssue() != null && link.getOutwardIssue().getKey() != null) {
					createDependency(req.getId(), link.getOutwardIssue().getKey(), link.getType().getName());
				}
				if (link.getInwardIssue() != null && link.getInwardIssue().getKey() != null) {
					createDependency(link.getInwardIssue().getKey(), req.getId(), link.getType().getName());
				}
			}
		}
	}

	/**
	 * Create new Dependency and save it to a List of Dependencies
	 * 
	 * @param reqFrom
	 *            String id of the Requirement from
	 * @param reqTo
	 *            String id of the Requirement to
	 * @param type
	 *            String type of the Dependency
	 */
	private void createDependency(String reqFrom, String reqTo, String type) {
		Dependency dependency = new Dependency();
		dependency.setFromid(reqFrom);
		dependency.setToid(reqTo);
		setDependencyType(dependency, type);
		dependency.setId(reqFrom + "_" + reqTo + "_" + dependency.getDependency_type());
		setStatusForDependency(dependency, "accepted");
		dependency.setCreated_at(new Date().getTime());
		dependency.setDescription(new ArrayList<String>());
		dependencies.add(dependency);
	}

	/**
	 * Adds children ("subRequirements") to a Requirement, Subtasks are received
	 * from an Issue, and converted into Requirements.
	 * 
	 * @param requirements
	 * @param req
	 * @param subtask
	 */
	private void addSubtask(Requirement req, Subtask subtask) {
		createDependency(req.getId(), subtask.getKey(), "subtask");
	}

	/**
	 * Jira Issues know their parents (Epics)
	 * (issue.getFields().getCustomfield10400()), and this method creates a
	 * Dependency between an Epic and its "child" (Dependency_type Decomposition).
	 * 
	 * @param requirements
	 * @param issue
	 * @param req
	 */
	private void updateParentEpic(HashMap<String, Requirement> requirements, Issue issue, Requirement req) {
		Object epicKeyObject = issue.getFields().getCustomfield10400();
		if (epicKeyObject.toString().equals("null")) {
			return; // No parent
		}
		String epicKey = cleanEpicKey(epicKeyObject.toString());
		createDependency(epicKey, req.getId(), "epic");

	}

	/**
	 * Helper method for cleaning "" marks from issue keys that belong to epics (no
	 * idea why epic issues seem to have extra "" around them)
	 * 
	 * @param epicKey
	 * @return
	 */
	private String cleanEpicKey(String epicKey) {
		char[] chars = epicKey.toCharArray();
		String newEpicKey = "";
		for (int i = 1; i < chars.length - 1; i++) {
			newEpicKey = newEpicKey + chars[i];
		}
		return newEpicKey;
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
		switch (type.toLowerCase()) {
		case "bug":
			req.setRequirement_type(Requirement_type.BUG);
			break;
		case "epic":
			req.setRequirement_type(Requirement_type.EPIC);
			break;
		case "initiative": // Not necessary?
			req.setRequirement_type(Requirement_type.INITIATIVE);
			break;
		case "sub-task":
			req.setRequirement_type(Requirement_type.TASK);
			break;
		case "suggestion":
			req.setRequirement_type(Requirement_type.ISSUE);
			break;
		case "task":
			req.setRequirement_type(Requirement_type.TASK);
			break;
		case "technical task":
			req.setRequirement_type(Requirement_type.TASK);
			break;
		case "change request":
			req.setRequirement_type(Requirement_type.NON_FUNCTIONAL);
			break;
		case "user story":
			req.setRequirement_type(Requirement_type.USER_STORY);
			break;
		}

		if (type.toLowerCase().contains("task")) {
			addExactTaskTypeToRequirementParts(req, type);
		}
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

		switch (status.toLowerCase()) {
		case "reported":
			req.setStatus(Requirement_status.SUBMITTED); // SUBMITTED = Todo in Qt system
			break;
		case "reopened":
			req.setStatus(Requirement_status.SUBMITTED);
			break;
		case "open":
			req.setStatus(Requirement_status.SUBMITTED);
			break;
		case "todo":
			req.setStatus(Requirement_status.SUBMITTED);
			break;
		case "accepted":
			req.setStatus(Requirement_status.SUBMITTED);
			break;
		case "blocked":
			req.setStatus(Requirement_status.DEFERRED); // DEFERRED = Stuck in Qt system
			break;
		case "need more info":
			req.setStatus(Requirement_status.DEFERRED);
			break;
		case "waiting for 3rd party":
			req.setStatus(Requirement_status.DEFERRED);
			break;
		case "on hold":
			req.setStatus(Requirement_status.DEFERRED);
			break;
		case "in progress":
			req.setStatus(Requirement_status.ACCEPTED); // ACCEPTED = In progress in Qt system
			break;
		case "implemented":
			req.setStatus(Requirement_status.ACCEPTED);
			break;
		case "withdrawn":
			req.setStatus(Requirement_status.COMPLETED); // COMPLETED = Done in Qt system
			break;
		case "verified":
			req.setStatus(Requirement_status.COMPLETED);
			break;
		case "closed":
			req.setStatus(Requirement_status.COMPLETED);
			break;
		case "resolved":
			req.setStatus(Requirement_status.COMPLETED);
			break;
		case "rejected":
			req.setStatus(Requirement_status.COMPLETED);
			break;
		case "done":
			req.setStatus(Requirement_status.COMPLETED);
			break;
		}

		addExactStatusToRequirementParts(req, status);
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

		switch (type.toLowerCase()) {
		case "dependency":
			dependency.setDependency_type(Dependency_type.REQUIRES);
			break;
		case "relates":
			dependency.setDependency_type(Dependency_type.CONTRIBUTES);
			break;
		case "duplicate":
			dependency.setDependency_type(Dependency_type.DUPLICATES);
			break;
		case "replacement":
			dependency.setDependency_type(Dependency_type.REPLACES);
			break;
		case "work breakdown":
			dependency.setDependency_type(Dependency_type.REFINES);
			break;
		case "test":
			dependency.setDependency_type(Dependency_type.REFINES);
			break;
		case "subtask":
			dependency.setDependency_type(Dependency_type.DECOMPOSITION);
			subtaskCount++;
			break;
		case "epic":
			dependency.setDependency_type(Dependency_type.DECOMPOSITION);
			epicCount++;
			break;
		}
	}

	/**
	 * Assigns Dependency_status to a Dependency
	 * 
	 * @param dependency
	 *            Dependency needing a Dependency_status
	 * @param status
	 */
	private void setStatusForDependency(Dependency dependency, String status) {

		switch (status.toLowerCase()) {
		case "accepted":
			dependency.setStatus(Dependency_status.ACCEPTED);
			break;
		case "proposed":
			dependency.setStatus(Dependency_status.PROPOSED);
			break;
		case "rejected":
			dependency.setStatus(Dependency_status.REJECTED);
			break;
		}
	}

	/**
	 * The exact status of a Qt Jira Issue will be saved to RequirementParts
	 * 
	 * @param req
	 * @param status
	 */
	private void addExactStatusToRequirementParts(Requirement req, String status) {
		RequirementPart reqPart = new RequirementPart();
		reqPart.setId(req.getId() + "_STATUS");
		reqPart.setName("Status");
		reqPart.setText(status);
		reqPart.setCreated_at(new Date().getTime());
		req.getRequirementParts().add(reqPart);

	}

	/**
	 * Qt Jira Issues have three different task-types (task, technical task,
	 * sub-task), and this info will be saved to RequirementParts
	 * 
	 * @param req
	 * @param taskType
	 */
	private void addExactTaskTypeToRequirementParts(Requirement req, String taskType) {
		RequirementPart reqPart = new RequirementPart();
		reqPart.setId(req.getId() + "_TASK");
		reqPart.setName("Task");
		reqPart.setText(taskType);
		reqPart.setCreated_at(new Date().getTime());
		req.getRequirementParts().add(reqPart);

	}

	/**
	 * Add information on the resolution of an issue to a RequirementPart object. If
	 * issue's resolution is null, create a new RequirementPart with the text
	 * "Unresolved"
	 * 
	 * @param issue
	 * @param req
	 */
	private void addResolutionToRequirementParts(Issue issue, Requirement req) {
		RequirementPart reqPart = new RequirementPart();
		reqPart.setId(req.getId() + "_RESOLUTION");
		reqPart.setName("Resolution");

		if (issue.getFields().getResolution() != null) {
			reqPart.setText(issue.getFields().getResolution().getName());
			reqPart.setCreated_at(new Date().getTime()); // Here issue.getFields().getResolutionDate()?

		} else {
			reqPart.setText("Unresolved");
			reqPart.setCreated_at(new Date().getTime());
		}
		req.getRequirementParts().add(reqPart);
	}

	/**
	 * 
	 * @param issue
	 * @param req
	 */
	private void addEnvironmentToRequirementParts(Issue issue, Requirement req) {
		RequirementPart reqPart = new RequirementPart();
		reqPart.setId(req.getId() + "_ENVIRONMENT");
		reqPart.setName("Environment");

		if (issue.getFields().getEnvironment() != null) {
			ObjectMapper mapper = new ObjectMapper();
			String environmentString;
			try {
				environmentString = mapper.writeValueAsString(issue.getFields().getEnvironment());
			//	System.out.println(environmentString);
			} catch (JsonProcessingException e) {
				environmentString = "";
				e.printStackTrace();
			}
			reqPart.setText(environmentString);
		}
		req.getRequirementParts().add(reqPart);
	}

	/**
	 * 
	 * @param issue
	 * @param req
	 */
	private void addLabelsToRequirementParts(Issue issue, Requirement req) {
		RequirementPart reqPart = new RequirementPart();
		reqPart.setId(req.getId() + "_LABELS");
		reqPart.setName("Labels");

		if (issue.getFields().getLabels() != null) {
			ObjectMapper mapper = new ObjectMapper();
			String labelString;
			try {
				labelString = mapper.writeValueAsString(issue.getFields().getLabels());
			} catch (JsonProcessingException e) {
				labelString = "";
				e.printStackTrace();
			}
			reqPart.setText(labelString);
		}
		req.getRequirementParts().add(reqPart);
	}

	/**
	 * 
	 * @param issue
	 * @param req
	 */
	private void addVersionsToRequirementParts(Issue issue, Requirement req) {
		RequirementPart reqPart = new RequirementPart();
		reqPart.setId(req.getId() + "_VERSIONS");
		reqPart.setName("Versions");

		if (issue.getFields().getVersions() != null && !issue.getFields().getVersions().isEmpty()) {
			List<Version> versions = issue.getFields().getVersions();
			List<String> names = new ArrayList<String>();
			for(Version version : versions) {
				names.add(version.getName());
			}
		//	System.out.println(issue.getFields().getVersions().get(0).getName());
			ObjectMapper mapper = new ObjectMapper();
			String versionsString;
			try {
				versionsString = mapper.writeValueAsString(names);
				//System.out.println(versionsString);
				//versionsString = mapper.writeValueAsString(issue.getFields().getVersions());
			} catch (JsonProcessingException e) {
				versionsString = "";
				e.printStackTrace();
			}
			reqPart.setText(versionsString);
		}
		req.getRequirementParts().add(reqPart);
	}

	/**
	 * 
	 * @param issue
	 * @param req
	 */
	private void addComponentsToRequirementParts(Issue issue, Requirement req) {
		RequirementPart reqPart = new RequirementPart();
		reqPart.setId(req.getId() + "_COMPONENTS");
		reqPart.setName("Components");

		if (issue.getFields().getComponents() != null && !issue.getFields().getComponents().isEmpty()) {
			List<Component> components = issue.getFields().getComponents();
			List<String> names = new ArrayList<String>();
			for(Component component : components) {
				names.add(component.getName());
			}
			ObjectMapper mapper = new ObjectMapper();
			String componentsString;
			try {
			//	componentsString = mapper.writeValueAsString(issue.getFields().getComponents());
				componentsString = mapper.writeValueAsString(names);
			//	System.out.println(componentsString);
			} catch (JsonProcessingException e) {
				componentsString = "";
				e.printStackTrace();
			}
			reqPart.setText(componentsString);
		}
		req.getRequirementParts().add(reqPart);
	}

	/**
	 * 
	 * @param issue
	 * @param req
	 */
	private void addPlatformsToRequirementParts(Issue issue, Requirement req) {
		RequirementPart reqPart = new RequirementPart();
		reqPart.setId(req.getId() + "_PLATFORMS");
		reqPart.setName("Platforms");
		//System.out.println("Issue is " + issue.getKey());
		//System.out.println("issue.getFields().getCustomfield11100() " + issue.getFields().getCustomfield11100());

		if (issue.getFields().getCustomfield11100() != null) {
			//System.out.println(issue.getFields().getCustomfield11100());
			ObjectMapper mapper = new ObjectMapper();
		//	Gson gson = new Gson();
			Platforms platforms = issue.getFields().getCustomfield11100(); //(issue.getFields().getCustomfield11100())
			List<String> labels = new ArrayList<String>();
			for(Platform platform : platforms.getplatforms()) {
				labels.add(platform.getLabel());
			}
		//	System.out.println(labels.get(0));
	//		JsonObject element = gson.toJson(issue.getFields().getCustomfield11100());
			String platformsString;
			try {
				platformsString = mapper.writeValueAsString(labels);
				//System.out.println("platformsString" + platformsString);
				//platformsString =  issue.getFields().getCustomfield11100().toString(); //(issue.getFields().getCustomfield11100());
			} catch (JsonProcessingException e) {
				platformsString = "";
				e.printStackTrace();
			}
			reqPart.setText(platformsString);
		}
		req.getRequirementParts().add(reqPart);
	}

	/**
	 * Adds only the latest fixVersion to a RequirementPart
	 * 
	 * @param issue
	 * @param req
	 */
	private void addFixVersionsToRequirementParts(Issue issue, Requirement req) {
		FixVersion fixVersion = getLatestFixVersion(issue);
		RequirementPart reqPart = new RequirementPart();
		reqPart.setName("FixVersion");
		if (fixVersion != null) {
			try {

				int number = fixVersions.get(fixVersion.getName()); // This number tells the "release number (or id)" of
																	// the fix version
				reqPart.setId(req.getId() + "_" + fixVersion.getId() + "_" + number);
				// reqPart.setName("FixVersion");
			//	ObjectMapper mapper = new ObjectMapper();
			//	String versionString = mapper.writeValueAsString(fixVersion.getDescription());
				String versionString = fixVersion.getDescription();
				reqPart.setText(versionString);
//				req.getRequirementParts().add(reqPart);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			reqPart.setId(req.getId() + "_FixVersion");
			reqPart.setText("No FixVersion");
		}
		req.getRequirementParts().add(reqPart);
	}

	/**
	 * Searches the map fixVersions to determine the latest FixVersion
	 * 
	 * @param issue
	 * @return
	 */
	private FixVersion getLatestFixVersion(Issue issue) {
		if (fixVersions == null) {
			return null;
		}
		FixVersion fixVersion = null;
		long latest = 0;
		int newest = fixVersions.size();
		if (issue.getFields().getFixVersions() != null && !issue.getFields().getFixVersions().isEmpty()) {
			for (FixVersion fixVersion2 : issue.getFields().getFixVersions()) {
				if (fixVersions.containsKey(fixVersion2.getName())) {
					if (newest > fixVersions.get(fixVersion2.getName())) {
						newest = fixVersions.get(fixVersion2.getName());
						fixVersion = fixVersion2;
					}
				}
			}
		}
		return fixVersion;
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

}
