package eu.openreq.milla.qtjiraimporter;

import com.google.gson.*;

import okhttp3.OkHttpClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;

/**
 * This class gets all the issues of a project. Since the issues are enumerated
 * continuously we can get the number of issues and iterate through ids of the
 * following form: PROJECTNAME-#issue. Since we get a higher number than the
 * actual amount of issues we get a few error messages which we ignore.
 */
public class ProjectIssues {

	// name of the project
	private String _project;
	// amount of issues in a project
	private int _maxProjectIssues;
	// the REST API URI
	private String _PROJECT_ISSUES_URL;

	public ProjectIssues(String project) throws IOException {
		_project = project;
		NumberOfIssuesHTML numberOfIssues = new NumberOfIssuesHTML(project);
		_maxProjectIssues = numberOfIssues.getNumberOfIssues();
		_PROJECT_ISSUES_URL = "https://bugreports.qt.io/rest/api/2/issue/" + _project + "-%d";
	}
	
	/**
	 * Collects issues that have keys between two numbers (start, end) from a specified project. 
	 * @param start Integer, starting point of the import
	 * @param end Integer, end point of the import
	 * @return List of JsonElements
	 * @throws IOException
	 */
	public Collection<JsonElement> collectIssues(int start, int end) throws IOException {
		OkHttpClient client = new OkHttpClient();
		Run run = new Run();
		Gson issueJSON = new Gson();
		
		List<String> paths = new ArrayList<>();
		for (int i = start; i <= end; i++) {
			// access the issue JSONs
			String requestURL = String.format(_PROJECT_ISSUES_URL, i);
			paths.add(requestURL);
			requestURL = null;
		}

		ConcurrentHashMap<String, JsonElement> issues = new ConcurrentHashMap<>();
		ForkJoinPool customThreadPool = new ForkJoinPool(32);
		try {
			customThreadPool.submit(
			        () -> paths.parallelStream().forEach((url) -> {
			        	String responseJSON = "";
						try {
							responseJSON = run.run(url, client);
						} catch (IOException e) {
							e.printStackTrace();
						}
						if (responseJSON != null) {
							JsonObject issueElement = issueJSON.fromJson(responseJSON, JsonElement.class).getAsJsonObject();
							String urlId = url.substring(url.lastIndexOf("/") + 1);
							String responseId = issueElement.get("key").getAsString();
							if (urlId.equals(responseId)) {
							issues.put(url, issueElement); 
							} 
//							else {
//								System.out.println(urlId + " is different from " + responseId + " !!");
//							}
							issueElement = null;
						}
			        })
			).get();
		} catch (InterruptedException | ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		paths.clear();
		return issues.values();
	
	}
	
	public int getNumberOfIssues() {
		return _maxProjectIssues;
	}

}
