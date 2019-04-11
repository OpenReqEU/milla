package eu.openreq.milla.qtjiraimporter;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import okhttp3.OkHttpClient;

public class UpdatedIssues {

	// HashMap to save in the Issue identifier and the JSON of the issue
	private HashMap<String, JsonElement> _projectIssues;
	// name of the project
	private String singleIssueUrl;

	public UpdatedIssues(String project) throws IOException {
		_projectIssues = new HashMap<String, JsonElement>();
		singleIssueUrl = "https://bugreports.qt.io/rest/api/2/search?jql=project=" + project + "+order+by+updated+DESC&maxResults=1&startAt=";
	}

	/**
	 * Fetches the 100th (possibly) updated Issue from Qt Jira
	 * @param start
	 * @return
	 * @throws IOException
	 */
	public JsonElement getTheLatestUpdatedIssue(int start) throws IOException {
		OkHttpClient client = new OkHttpClient();
		Run run = new Run();

		int i = start;
		String requestURL = singleIssueUrl + i;
		String responseJSON = run.run(requestURL, client);
		Gson gson = new Gson();
		JsonObject projectJSON = null;
		JsonElement element = null;
		if (responseJSON != null) {
			projectJSON = gson.fromJson(responseJSON, JsonElement.class).getAsJsonObject();
			JsonArray issuesInProjectJSON = projectJSON.getAsJsonArray("issues");
			if (issuesInProjectJSON.size()>0) {
				element = issuesInProjectJSON.get(0);
			}
		}
		return element;
	}

	/**
	 * Fetches ALL updated Issues from Qt Jira
	 * @param project
	 * @param amount
	 * @throws IOException
	 */
	public void collectAllUpdatedIssues(String project, int current) throws IOException {
		OkHttpClient client = new OkHttpClient();
		Run run = new Run();
		
		String projectIssuesUrl = "https://bugreports.qt.io/rest/api/2/search?jql=project=" + project
				+ "+order+by+updated+DESC&maxResults=1000&startAt=" + current;

		String responseJSON = run.run(projectIssuesUrl, client);
		if(responseJSON!=null) {
			Gson gson = new Gson();
			JsonObject projectJSON = gson.fromJson(responseJSON, JsonElement.class).getAsJsonObject();
			JsonArray issuesInProjectJSON = projectJSON.getAsJsonArray("issues");
			for (int i = 0; i < issuesInProjectJSON.size(); i++) {
				JsonObject issueJSON = issuesInProjectJSON.get(i).getAsJsonObject();
					String issueKey = issueJSON.get("key").getAsString();
					_projectIssues.put(issueKey, issueJSON);
			}
		}
	}

	public Collection<JsonElement> getProjectIssues() {
		return _projectIssues.values();
	}
	
	public void clearIssues() {
		_projectIssues.clear();
	}
}
