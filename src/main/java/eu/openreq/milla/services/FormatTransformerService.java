package eu.openreq.milla.services;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;

import eu.openreq.milla.models.entity.IssueObject;
import eu.openreq.milla.models.jira.Fields;
import eu.openreq.milla.models.jira.Issue;
import eu.openreq.milla.models.jira.Issuelink;
import eu.openreq.milla.models.jira.Jira;
import eu.openreq.milla.models.jira.Priority;
import eu.openreq.milla.models.jira.Status;
import eu.openreq.milla.models.jira.Status__;
import eu.openreq.milla.models.jira.Subtask;
import eu.openreq.milla.models.mulson.Attribute;
import eu.openreq.milla.models.mulson.Relationship;
import eu.openreq.milla.models.mulson.Requirement;
import eu.openreq.milla.models.mulson.SubFeature;
import eu.openreq.milla.repositories.IssueRepository;

/**
 * Methods used to convert between formats
 * 
 * @author iivorait
 * @author tlaurinen
 */
@Service
public class FormatTransformerService {
	
	private List<IssueObject> issueObjects;

	// /**
	// * Not used anymore
	// * @param jiras
	// * Class files made with http://www.jsonschema2pojo.org/
	// * @return
	// */
	// public Collection<Requirement> convertJirasToMulson(Collection<Jira> jiras) {
	// HashMap<String, Requirement> requirements = new HashMap<>();
	//
	// for (Jira jira : jiras) {
	// for (Issue issue : jira.getIssues()) {
	// Requirement req = new Requirement();
	// req.setRequirementId(issue.getKey().replace("-", "_")); // Kumbang doesn't
	// like hyphens
	// req.setName(issue.getFields().getSummary());
	// requirements.put(req.getRequirementId(), req);
	//
	// addAttribute(req, "priority", issue.getFields().getPriority().getId());
	// addAttribute(req, "status", issue.getFields().getStatus().getName());
	//
	// addRequiredRelationships(issue, req);
	//
	// updateParentEpic(requirements, issue, req);
	//
	// List<Subtask> subtasks = issue.getFields().getSubtasks();
	// if (subtasks != null && !subtasks.isEmpty()) {
	// for (Subtask subtask : subtasks) {
	// addSubtask(requirements, req, subtask);
	// }
	// }
	// }
	// }
	// return requirements.values();
	// }

	/**
	 * Converts JsonElement objects to Issue Objects and adds "mock" issues to the
	 * Issue list to replace the issues that are linked to the project's issues, but
	 * are not in the same project
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
		Set<String> allIssueKeys = new HashSet<>();
		Set<String> linkedProjectIssueKeys = new HashSet<>();
		issueObjects = new ArrayList<>();

		System.out.println("Starting to create Issues and saving to database");

		for (int i = 0; i < jsonElements.size(); i++) {
			JsonElement element = jsonElements.get(i);
			Issue issue = gson.fromJson(element, Issue.class);
			issues.add(issue);
			allIssueKeys.add(issue.getKey());
			IssueObject issueObject = createNewIssueObject(issue, gson.toJson(element));
			issueObjects.add(issueObject);

			// if (i%1000==0) {
			// System.out.println("i is "+ i);
			// }

			if (issue.getFields() != null) {
				if (!issue.getFields().getIssuelinks().isEmpty()) {
					for (int j = 0; j < issue.getFields().getIssuelinks().size(); j++) {
						if (issue.getFields().getIssuelinks().get(j).getInwardIssue() != null) {
							String inward = issue.getFields().getIssuelinks().get(j).getInwardIssue().getKey();
							linkedProjectIssueKeys.add(inward);
						}
						if (issue.getFields().getIssuelinks().get(j).getOutwardIssue() != null) {
							String outward = issue.getFields().getIssuelinks().get(j).getOutwardIssue().getKey();
							linkedProjectIssueKeys.add(outward);
						}
					}
				}
			}
			element = null;
			issue = null;
			issueObject = null;
		}

		System.out.println("IssueObject list created");
		int i = 1;
		linkedProjectIssueKeys.removeAll(allIssueKeys); // This leaves to the set of linked issues only those issues
														// that are in a different project
		System.out.println("Issue creation and saving complete");
		for (String key : linkedProjectIssueKeys) {
			Issue otherIssue = createMockIssue(key, i);
			issues.add(otherIssue);
			i++;
		}
		System.out.println("MockIssues added");
		long end = System.nanoTime();
		long durationSec = (end - start) / 1000000000;
		double durationMin = durationSec / 60.0;

		System.out
				.println("Lists done, it took " + durationSec + " second(s) or " + durationMin + " minute(s).");

		return issues;
	}

	// public List<Issue> convertJsonElementsToIssues(List<JsonElement>
	// jsonElements, List<String> responses, String projectId)
	// throws IOException {
	// long start = System.nanoTime();
	// List<Issue> issues = new ArrayList<>();
	// Gson gson = new GsonBuilder().create();
	// Type listType = new TypeToken<ArrayList<Issue>>(){}.getType();
	// //String issueString = gson.toJson(jsonElements);
	// //System.out.println(issueString);
	// issues = gson.fromJson(gson.toJson(jsonElements), listType);
	// System.out.println("List of issues created ");
	//
	// issues = addMockIssuesToList(issues, responses);
	// System.out.println("MockIssues added");
	// long end = System.nanoTime();
	// long durationSec = (end - start) / 1000000000;
	// double durationMin = durationSec / 60.0;
	// System.out.println("Saving to DB done, it took " + durationSec + " second(s)
	// or " + durationMin + " minute(s).");
	// return issues;
	// }
	//
	// private List<Issue> addMockIssuesToList(List<Issue>issues, List<String>
	// responses) {
	// Set<String> allIssueKeys = new HashSet<>();
	// Set<String> linkedProjectIssueKeys = new HashSet<>();
	// List<IssueObject> issueObjects = new ArrayList<>();
	// for(int i = 0; i <issues.size(); i++) {
	// Issue issue = issues.get(i);
	// allIssueKeys.add(issue.getKey());
	// issueObjects.add(createNewIssueObject(issue, responses.get(i)));
	// if (issue.getFields() != null) {
	// if (!issue.getFields().getIssuelinks().isEmpty()) {
	// for (int j = 0; j < issue.getFields().getIssuelinks().size(); j++) {
	// if (issue.getFields().getIssuelinks().get(j).getInwardIssue() != null) {
	// String inward =
	// issue.getFields().getIssuelinks().get(j).getInwardIssue().getKey();
	// linkedProjectIssueKeys.add(inward);
	// }
	// if (issue.getFields().getIssuelinks().get(j).getOutwardIssue() != null) {
	// String outward =
	// issue.getFields().getIssuelinks().get(j).getOutwardIssue().getKey();
	// linkedProjectIssueKeys.add(outward);
	// }
	// }
	//
	// }
	// }
	// }
	// issueRepository.save(issueObjects);
	// int i = 1;
	// linkedProjectIssueKeys.removeAll(allIssueKeys); // This leaves to the set of
	// linked issues only those issues that are in a different project
	// System.out.println("Issue creation and saving complete");
	// for (String key : linkedProjectIssueKeys) {
	// Issue otherIssue = createMockIssue(key, i);
	// issues.add(otherIssue);
	// i++;
	// }
	// return issues;
	// }

//	/**
//	 * Converts JsonElement objects to Issue Objects and adds "mock" issues to the
//	 * Issue list to replace the issues that are linked to the project's issues, but
//	 * are not in the same project
//	 * 
//	 * @param jsonElements
//	 *            a collection of JsonElement objects
//	 * @return a List of Issue objects
//	 * @throws IOException
//	 */
//	public List<Issue> convertJsonElementsToIssuesLargeProject(List<JsonElement> jsonElements, String projectId)
//			throws IOException {
//		List<Issue> issues = new ArrayList<>();
//		Gson gson = new Gson();
//
//		long start = System.nanoTime();
//
//		Set<String> allIssueKeys = new HashSet<>();
//		Set<String> linkedProjectIssueKeys = new HashSet<>();
//
//		int endpoint = jsonElements.size() / 100;
//		int starting = 0;
//		int sum = endpoint;
//		System.out.println("Starting to create Issues and saving to database");
//
//		for (int index = 1; index < 100; index++) {
//			List<IssueObject> issueObjects = new ArrayList<>();
//			for (int i = starting; i < sum; i++) {
//				JsonElement element = jsonElements.get(i);
//				Issue issue = gson.fromJson(element, Issue.class);
//				issues.add(issue);
//				allIssueKeys.add(issue.getKey());
//				issueObjects.add(createNewIssueObject(issue, gson.toJson(element)));
//
//				if (issue.getFields() != null) {
//					if (!issue.getFields().getIssuelinks().isEmpty()) {
//						for (int j = 0; j < issue.getFields().getIssuelinks().size(); j++) {
//							if (issue.getFields().getIssuelinks().get(j).getInwardIssue() != null) {
//								String inward = issue.getFields().getIssuelinks().get(j).getInwardIssue().getKey();
//								linkedProjectIssueKeys.add(inward);
//							}
//							if (issue.getFields().getIssuelinks().get(j).getOutwardIssue() != null) {
//								String outward = issue.getFields().getIssuelinks().get(j).getOutwardIssue().getKey();
//								linkedProjectIssueKeys.add(outward);
//							}
//						}
//					}
//				}
//				element = null;
//				issue = null;
//			}
//			//issueRepository.save(issueObjects);
//			issueObjects = null;
//			System.out.println("Index is " + index);
//			starting = sum;
//			sum = sum + endpoint;
//		}
//
//		System.out.println("Last part started");
//		List<IssueObject> issueObjects = new ArrayList<>();
//		for (int i = starting; i < jsonElements.size(); i++) {
//			JsonElement element = jsonElements.get(i);
//			String elementString = gson.toJson(element);
//			Issue issue = gson.fromJson(element, Issue.class);
//			issues.add(issue);
//			allIssueKeys.add(issue.getKey());
//			issueObjects.add(createNewIssueObject(issue, elementString));
//
//			if (issue.getFields() != null) {
//				if (!issue.getFields().getIssuelinks().isEmpty()) {
//					for (int j = 0; j < issue.getFields().getIssuelinks().size(); j++) {
//						if (issue.getFields().getIssuelinks().get(j).getInwardIssue() != null) {
//							String inward = issue.getFields().getIssuelinks().get(j).getInwardIssue().getKey();
//							linkedProjectIssueKeys.add(inward);
//						}
//						if (issue.getFields().getIssuelinks().get(j).getOutwardIssue() != null) {
//							String outward = issue.getFields().getIssuelinks().get(j).getOutwardIssue().getKey();
//							linkedProjectIssueKeys.add(outward);
//						}
//					}
//				}
//			}
//		}
//
//		System.out.println("IssueObject list created");
//		//issueRepository.save(issueObjects);
//	//	System.out.println("List saved, IssueRepo has " + issueRepository.count());
//		int i = 1;
//		linkedProjectIssueKeys.removeAll(allIssueKeys); // This leaves to the set of linked issues only those issues
//														// that are in a different project
//		System.out.println("Issue creation and saving complete");
//		for (String key : linkedProjectIssueKeys) {
//			Issue otherIssue = createMockIssue(key, i);
//			issues.add(otherIssue);
//			i++;
//		}
//		System.out.println("MockIssues added");
//		long end = System.nanoTime();
//		long durationSec = (end - start) / 1000000000;
//		double durationMin = durationSec / 60.0;
//
//		System.out
//				.println("Saving to DB done, it took " + durationSec + " second(s) or " + durationMin + " minute(s).");
//
//		return issues;
//	}
	
	// /**
	// * Converts JsonElement objects to Issue Objects and adds "mock" issues to the
	// * Issue list to replace the issues that are linked to the project's issues,
	// but
	// * are not in the same project
	// *
	// * @param jsonElements
	// * a collection of JsonElement objects
	// * @return a List of Issue objects
	// * @throws IOException
	// */
	// public List<Issue> convertJsonElementsToIssues(List<JsonElement>
	// jsonElements, String projectId) //List<String> responses,
	// throws IOException {
	//
	// List<Issue> issues = new ArrayList<>();
	// Gson gson = new Gson();
	//
	// long start = System.nanoTime();
	//
	// // Printing all issues to a file for testing
	//// String fileName = "" + projectId + "_issues.txt";
	// // File name and path must be added if a log file of the issues is needed
	//// FileWriter fileWriter = new FileWriter(fileName);
	//// PrintWriter printWriter = new PrintWriter(fileWriter);
	//// String newLine = System.getProperty("line.separator");
	// Set<String> allIssueKeys = new HashSet<>();
	// Set<String> linkedProjectIssueKeys = new HashSet<>();
	// List<IssueObject> issueObjects = new ArrayList<>();
	// System.out.println("Starting to create Issues and saving to database");
	// for (int i = 0; i<jsonElements.size(); i++) {
	// JsonElement element = jsonElements.get(i);
	// Issue issue = gson.fromJson(element, Issue.class);
	// issues.add(issue);
	// allIssueKeys.add(issue.getKey());
	// IssueObject issueObject = createNewIssueObject(issue, gson.toJson(element));
	// issueObjects.add(issueObject);
	//
	// if (i%1000==0) {
	// System.out.println("i is "+ i);
	// }
	// //Create a new IssueObject based on the Issue and JsonElement and save
	//// if(issueRepository.findByKey(issue.getKey())==null) {
	//// saveIssueObjectToDatabase(issue, element);
	//// }
	// // The following lines are here for getting all linked issues to their own
	// sets
	// // and for printing all issues to a file
	// if (issue.getFields() != null) {
	// if (!issue.getFields().getIssuelinks().isEmpty()) {
	//// printWriter.print(issue.getKey() + "\t" + "issue links are" + "\t");
	// for (int j = 0; j < issue.getFields().getIssuelinks().size(); j++) {
	// if (issue.getFields().getIssuelinks().get(j).getInwardIssue() != null) {
	// String inward =
	// issue.getFields().getIssuelinks().get(j).getInwardIssue().getKey();
	// linkedProjectIssueKeys.add(inward);
	//
	//// printWriter.print("inward issue" + "\t"
	//// + issue.getFields().getIssuelinks().get(j).getInwardIssue().getKey() + "\t"
	//// + "issueLink type" + "\t"
	//// + issue.getFields().getIssuelinks().get(j).getType().getName() + "\t"
	//// + "inward "+ "\t"
	//// +issue.getFields().getIssuelinks().get(j).getType().getInward() + "\t"
	//// + "outward "+ "\t"
	//// +issue.getFields().getIssuelinks().get(j).getType().getOutward() + "\t");
	// }
	// if (issue.getFields().getIssuelinks().get(j).getOutwardIssue() != null) {
	// String outward =
	// issue.getFields().getIssuelinks().get(j).getOutwardIssue().getKey();
	// linkedProjectIssueKeys.add(outward);
	//
	//// printWriter.print("outward issue" + "\t"
	//// + issue.getFields().getIssuelinks().get(j).getOutwardIssue().getKey() +
	// "\t"
	//// + "issueLink type" + "\t"
	//// + issue.getFields().getIssuelinks().get(j).getType().getName() + "\t"
	//// + "inward: "+ "\t"
	//// +issue.getFields().getIssuelinks().get(j).getType().getInward() + "\t"
	//// + "outward: "+ "\t"
	//// +issue.getFields().getIssuelinks().get(j).getType().getOutward() + "\t");
	// }
	// }
	//// printWriter.print(newLine);
	// }
	//// else {
	//// printWriter.print(issue.getKey() + "\t" + "no issue links" + newLine);
	//// }
	// }
	// element = null;
	// issue= null;
	// issueObject= null;
	// }
	//// printWriter.close();
	// System.out.println("IssueObject list created");
	// issueRepository.save(issueObjects);
	// System.out.println("List saved, IssueRepo has " +issueRepository.count());
	// int i = 1;
	// linkedProjectIssueKeys.removeAll(allIssueKeys); // This leaves to the set of
	// linked issues only those issues that are in a different project
	// System.out.println("Issue creation and saving complete");
	// for (String key : linkedProjectIssueKeys) {
	// Issue otherIssue = createMockIssue(key, i);
	// issues.add(otherIssue);
	// i++;
	// }
	// System.out.println("MockIssues added");
	// long end = System.nanoTime();
	// long durationSec = (end - start) / 1000000000;
	// double durationMin = durationSec / 60.0;
	//
	// System.out.println("Saving to DB done, it took " + durationSec + " second(s)
	// or " + durationMin + " minute(s).");
	//
	//
	// return issues;
	// }

	/**
	 * Converts a List of Issue objects into Mulson Requirements
	 * 
	 * @param issues
	 *            List of Issue objects
	 * @return a collection of Requirement objects
	 */
	public Collection<Requirement> convertIssuesToMulson(List<Issue> issues, String projectId) throws Exception {
		HashMap<String, Requirement> requirements = new HashMap<>();
		for (Issue issue : issues) {
			try {
				Requirement req = new Requirement();
				req.setRequirementId(issue.getKey().replace("-", "_")); // Kumbang doesn't like hyphens
				String name = fixSpecialCharacters(issue.getFields().getSummary());
				req.setName(name);
				requirements.put(req.getRequirementId(), req);

				addAttribute(req, "priority", issue.getFields().getPriority().getId());
				addAttribute(req, "status", issue.getFields().getStatus().getName());

				addRequiredRelationships(issue, req);
				updateParentEpic(requirements, issue, req);

				List<Subtask> subtasks = issue.getFields().getSubtasks();
				if (subtasks != null && !subtasks.isEmpty()) {
					for (Subtask subtask : subtasks) {
						addSubtask(requirements, req, subtask);
					}
				}
			} catch (Exception e) {
				System.out.println("Error " + e);
				// e.printStackTrace();
			}
		}
		return requirements.values();
	}

	private String fixSpecialCharacters(String name) {
		String fixedName = name.replaceAll("[^\\x20-\\x7e]", ""); // TODO This is a quick fix, must be modified into a
																	// better version
		return fixedName;
	}

	private void addSubtask(HashMap<String, Requirement> requirements, Requirement req, Subtask subtask) {
		Requirement req2 = new Requirement();
		req2.setRequirementId(subtask.getKey().replace("-", "_"));
		req2.setName(subtask.getFields().getSummary());
		requirements.put(req2.getRequirementId(), req2);

		addAttribute(req2, "priority", subtask.getFields().getPriority().getId());
		addAttribute(req2, "status", subtask.getFields().getStatus().getName());

		SubFeature subfeat = new SubFeature();
		ArrayList<String> types = new ArrayList<>();
		types.add(req2.getRequirementId());
		subfeat.setTypes(types);
		subfeat.setRole(req2.getRequirementId());
		subfeat.setCardinality("0-1");

		req.getSubfeatures().add(subfeat);

		// No issue links (requirements)?
	}

	private void updateParentEpic(HashMap<String, Requirement> requirements, Issue issue, Requirement req) {
		Object epicKeyObject = issue.getFields().getCustomfield10400();
		if (epicKeyObject == null) {
			return; // No parent
		}
		String epicKey = epicKeyObject.toString().replace("-", "_");

		Requirement epic = requirements.get(epicKey);

		if (epic == null) { // Parent not yet created
			epic = new Requirement();
			epic.setRequirementId(epicKey);
			epic.setName("Epic " + epicKey);
			requirements.put(epicKey, epic);
		}

		SubFeature subfeat = new SubFeature();
		ArrayList<String> types = new ArrayList<>();
		types.add(req.getRequirementId());
		subfeat.setTypes(types);
		subfeat.setRole(req.getRequirementId());
		subfeat.setCardinality("0-1");

		epic.getSubfeatures().add(subfeat);
	}

	private void addRequiredRelationships(Issue issue, Requirement req) {
		if (issue.getFields().getIssuelinks() != null) {
			for (Issuelink link : issue.getFields().getIssuelinks()) {
				if (!"depends on".equals(link.getType().getOutward())) {
					continue;
				}
				if (link.getOutwardIssue() == null || link.getOutwardIssue().getKey() == null) {
					continue;
				}
				Relationship rel = new Relationship();
				req.getRelationships().add(rel);
				rel.setTargetId(link.getOutwardIssue().getKey().replace("-", "_"));
				rel.setType("requires");
			}
		}
	}

	private void addAttribute(Requirement req, String name, String value) {
		try {
			Attribute priority = new Attribute();
			priority.setName(name);
			ArrayList<String> priorities = new ArrayList<>();
			priorities.add(value.replace(" ", "_")); // Kumbang doesn't like spaces, either
			priority.setValues(priorities);
			req.getAttributes().add(priority);
		} catch (Exception e) {
			System.out.println("No " + name);
		}
	}

	/**
	 * Method for creating a "mock" Issue of Issues that are mentioned in a
	 * project's Issues' issueLinks, but do not belong to the same project.
	 * 
	 * @param key
	 *            of the Issue associated with the project's issues, a real
	 *            key/identifier
	 * @param i
	 *            an index used for creating mock identifiers
	 * @return Issue with made-up fields etc
	 */
	private Issue createMockIssue(String key, int i) {
		Issue otherIssue = new Issue();
		otherIssue.setKey(key);
		otherIssue.setExpand("");
		Fields fields = new Fields();
		Priority priority = new Priority();
		priority.setId("" + i);
		Status__ status = new Status__();
		status.setName("mockstatus");
		fields.setSummary("A mock issue");
		fields.setPriority(priority);
		fields.setStatus(status);
		fields.setSubtasks(null);
		otherIssue.setFields(fields);
		otherIssue.setId("" + i + 1);
		otherIssue.setSelf("");

		return otherIssue;
	}

	/**
	 * Creates an IssueObject
	 * 
	 * @param issue
	 * @param element
	 */
	private IssueObject createNewIssueObject(Issue issue, String element) {
		IssueObject issueObject = new IssueObject();
		issueObject.setKey(issue.getKey());
		issueObject.setIssueId(issue.getId());
		issueObject.setContent(element);
		issueObject.setUpdated(issue.getFields().getUpdated());
//		issueObject.setTimestamp(LocalDateTime.now());
		return issueObject;
	}
	
	public List<IssueObject> getIssueObjects() {
		return issueObjects;
	}
}
