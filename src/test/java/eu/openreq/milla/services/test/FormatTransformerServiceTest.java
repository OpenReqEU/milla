package eu.openreq.milla.services.test;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import eu.openreq.milla.models.jira.Issue;
import eu.openreq.milla.models.json.*;
import eu.openreq.milla.services.FormatTransformerService;

import static org.junit.Assert.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class FormatTransformerServiceTest {
	
	private FormatTransformerService transformerService;
	private List<JsonElement> testElements;
	private String projectId;
	private Person person;
	
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() throws IOException {
    	transformerService = new FormatTransformerService();
    	testElements = new ArrayList<JsonElement>();
    	projectId = "PROJECTID";
    	person = new Person();
    	person.setEmail("person@company.fi");
    	person.setUsername("person");
    	Gson issueJSON = new Gson();
    	String jsonString1 = "{\"expand\":\"renderedFields,names,schema,operations,editmeta,changelog,versionedRepresentations\",\"id\":\"236619\",\"self\":\"https://bugreports.qt.io/rest/api/2/issue/236619\",\"key\":\"QTWB-3\",\"fields\":{\"issuetype\":{\"self\":\"https://bugreports.qt.io/rest/api/2/issuetype/15\",\"id\":\"15\",\"description\":\"Public suggestions\",\"iconUrl\":\"https://bugreports.qt.io/secure/viewavatar?size=xsmall&avatarId=12170&avatarType=issuetype\",\"name\":\"Suggestion\",\"subtask\":false,\"avatarId\":12170},\"customfield_10190\":null,\"timespent\":null,\"project\":{\"self\":\"https://bugreports.qt.io/rest/api/2/project/11441\",\"id\":\"11441\",\"key\":\"QTWB\",\"name\":\"Qt WebBrowser\",\"avatarUrls\":{\"48x48\":\"https://bugreports.qt.io/secure/projectavatar?pid=11441&avatarId=10011\",\"24x24\":\"https://bugreports.qt.io/secure/projectavatar?size=small&pid=11441&avatarId=10011\",\"16x16\":\"https://bugreports.qt.io/secure/projectavatar?size=xsmall&pid=11441&avatarId=10011\",\"32x32\":\"https://bugreports.qt.io/secure/projectavatar?size=medium&pid=11441&avatarId=10011\"},\"projectCategory\":{\"self\":\"https://bugreports.qt.io/rest/api/2/projectCategory/10010\",\"id\":\"10010\",\"description\":\"Active projects\",\"name\":\"Active\"}},\"fixVersions\":[],\"aggregatetimespent\":null,\"resolution\":null,\"customfield_10302\":null,\"resolutiondate\":null,\"workratio\":-1,\"customfield_10906\":null,\"customfield_10907\":null,\"customfield_10908\":null,\"customfield_10909\":null,\"lastViewed\":null,\"watches\":{\"self\":\"https://bugreports.qt.io/rest/api/2/issue/QTWB-3/watchers\",\"watchCount\":1,\"isWatching\":false},\"customfield_10180\":null,\"customfield_10181\":null,\"created\":\"2016-08-05T05:46:58.000+0000\",\"customfield_10142\":null,\"priority\":{\"self\":\"https://bugreports.qt.io/rest/api/2/priority/3\",\"iconUrl\":\"https://bugreports.qt.io/images/icons/priorities/major.svg\",\"name\":\"P2: Important\",\"id\":\"3\"},\"customfield_10300\":\"9223372036854775807\",\"labels\":[],\"customfield_10301\":\"9223372036854775807\",\"timeestimate\":null,\"aggregatetimeoriginalestimate\":null,\"versions\":[{\"self\":\"https://bugreports.qt.io/rest/api/2/version/15908\",\"id\":\"15908\",\"description\":\"1.0\",\"name\":\"1.0\",\"archived\":false,\"released\":false}],\"issuelinks\":[],\"assignee\":{\"self\":\"https://bugreports.qt.io/rest/api/2/user?username=qt_webengine_team\",\"name\":\"qt_webengine_team\",\"key\":\"qt_webengine_team\",\"avatarUrls\":{\"48x48\":\"https://bugreports.qt.io/secure/useravatar?ownerId=qt_webengine_team&avatarId=13712\",\"24x24\":\"https://bugreports.qt.io/secure/useravatar?size=small&ownerId=qt_webengine_team&avatarId=13712\",\"16x16\":\"https://bugreports.qt.io/secure/useravatar?size=xsmall&ownerId=qt_webengine_team&avatarId=13712\",\"32x32\":\"https://bugreports.qt.io/secure/useravatar?size=medium&ownerId=qt_webengine_team&avatarId=13712\"},\"displayName\":\"Qt WebEngine Team\",\"active\":true,\"timeZone\":\"Europe/Berlin\"},\"updated\":\"2017-11-06T12:05:22.000+0000\",\"status\":{\"self\":\"https://bugreports.qt.io/rest/api/2/status/10011\",\"description\":\"The issue has been reported, but no validation has been done on it.\",\"iconUrl\":\"https://bugreports.qt.io/images/icons/statuses/email.png\",\"name\":\"Reported\",\"id\":\"10011\",\"statusCategory\":{\"self\":\"https://bugreports.qt.io/rest/api/2/statuscategory/2\",\"id\":2,\"key\":\"new\",\"colorName\":\"blue-gray\",\"name\":\"To Do\"}},\"components\":[{\"self\":\"https://bugreports.qt.io/rest/api/2/component/22527\",\"id\":\"22527\",\"name\":\"General\"}],\"timeoriginalestimate\":null,\"description\":\"Pinch-zooming on a touchscreen works, but I was expecting Ctrl-+/- and Ctrl-mousewheel to work too.\",\"timetracking\":{},\"customfield_10600\":null,\"customfield_10601\":null,\"customfield_10800\":\"0|i0ah6n:00000000001o\",\"attachment\":[],\"aggregatetimeestimate\":null,\"summary\":\"add shortcuts for zooming\",\"creator\":{\"self\":\"https://bugreports.qt.io/rest/api/2/user?username=srutledg\",\"name\":\"srutledg\",\"key\":\"srutledg\",\"avatarUrls\":{\"48x48\":\"https://bugreports.qt.io/secure/useravatar?ownerId=srutledg&avatarId=12270\",\"24x24\":\"https://bugreports.qt.io/secure/useravatar?size=small&ownerId=srutledg&avatarId=12270\",\"16x16\":\"https://bugreports.qt.io/secure/useravatar?size=xsmall&ownerId=srutledg&avatarId=12270\",\"32x32\":\"https://bugreports.qt.io/secure/useravatar?size=medium&ownerId=srutledg&avatarId=12270\"},\"displayName\":\"Shawn Rutledge\",\"active\":true,\"timeZone\":\"Europe/Oslo\"},\"subtasks\":[],\"reporter\":{\"self\":\"https://bugreports.qt.io/rest/api/2/user?username=srutledg\",\"name\":\"srutledg\",\"key\":\"srutledg\",\"avatarUrls\":{\"48x48\":\"https://bugreports.qt.io/secure/useravatar?ownerId=srutledg&avatarId=12270\",\"24x24\":\"https://bugreports.qt.io/secure/useravatar?size=small&ownerId=srutledg&avatarId=12270\",\"16x16\":\"https://bugreports.qt.io/secure/useravatar?size=xsmall&ownerId=srutledg&avatarId=12270\",\"32x32\":\"https://bugreports.qt.io/secure/useravatar?size=medium&ownerId=srutledg&avatarId=12270\"},\"displayName\":\"Shawn Rutledge\",\"active\":true,\"timeZone\":\"Europe/Oslo\"},\"aggregateprogress\":{\"progress\":0,\"total\":0},\"customfield_10200\":\"9223372036854775807\",\"customfield_10400\":null,\"environment\":\"Arch Linux, Qt 5.7 from the packages, qtwebbrowser from AUR\",\"customfield_10910\":null,\"customfield_10911\":null,\"progress\":{\"progress\":0,\"total\":0},\"comment\":{\"comments\":[],\"maxResults\":0,\"total\":0,\"startAt\":0},\"votes\":{\"self\":\"https://bugreports.qt.io/rest/api/2/issue/QTWB-3/votes\",\"votes\":0,\"hasVoted\":false},\"worklog\":{\"startAt\":0,\"maxResults\":20,\"total\":0,\"worklogs\":[]}}}";
    	JsonObject issueElement = issueJSON.fromJson(jsonString1, JsonElement.class).getAsJsonObject();
    	String jsonString2 = "{\"expand\":\"renderedFields,names,schema,operations,editmeta,changelog,versionedRepresentations\",\"id\":\"236620\",\"self\":\"https://bugreports.qt.io/rest/api/2/issue/236620\",\"key\":\"QTWB-4\",\"fields\":{\"issuetype\":{\"self\":\"https://bugreports.qt.io/rest/api/2/issuetype/15\",\"id\":\"15\",\"description\":\"Public suggestions\",\"iconUrl\":\"https://bugreports.qt.io/secure/viewavatar?size=xsmall&avatarId=12170&avatarType=issuetype\",\"name\":\"Suggestion\",\"subtask\":false,\"avatarId\":12170},\"customfield_10190\":null,\"timespent\":null,\"project\":{\"self\":\"https://bugreports.qt.io/rest/api/2/project/11441\",\"id\":\"11441\",\"key\":\"QTWB\",\"name\":\"Qt WebBrowser\",\"avatarUrls\":{\"48x48\":\"https://bugreports.qt.io/secure/projectavatar?pid=11441&avatarId=10011\",\"24x24\":\"https://bugreports.qt.io/secure/projectavatar?size=small&pid=11441&avatarId=10011\",\"16x16\":\"https://bugreports.qt.io/secure/projectavatar?size=xsmall&pid=11441&avatarId=10011\",\"32x32\":\"https://bugreports.qt.io/secure/projectavatar?size=medium&pid=11441&avatarId=10011\"},\"projectCategory\":{\"self\":\"https://bugreports.qt.io/rest/api/2/projectCategory/10010\",\"id\":\"10010\",\"description\":\"Active projects\",\"name\":\"Active\"}},\"fixVersions\":[],\"aggregatetimespent\":null,\"resolution\":null,\"customfield_10302\":null,\"resolutiondate\":null,\"workratio\":-1,\"customfield_10906\":null,\"customfield_10907\":null,\"customfield_10908\":null,\"customfield_10909\":null,\"lastViewed\":null,\"watches\":{\"self\":\"https://bugreports.qt.io/rest/api/2/issue/QTWB-4/watchers\",\"watchCount\":4,\"isWatching\":false},\"customfield_10180\":null,\"customfield_10181\":null,\"created\":\"2016-08-05T05:49:41.000+0000\",\"customfield_10142\":null,\"priority\":{\"self\":\"https://bugreports.qt.io/rest/api/2/priority/4\",\"iconUrl\":\"https://bugreports.qt.io/images/icons/priorities/minor.svg\",\"name\":\"P3: Somewhat important\",\"id\":\"4\"},\"customfield_10300\":\"9223372036854775807\",\"labels\":[],\"customfield_10301\":\"9223372036854775807\",\"timeestimate\":null,\"aggregatetimeoriginalestimate\":null,\"versions\":[],\"issuelinks\":[],\"assignee\":{\"self\":\"https://bugreports.qt.io/rest/api/2/user?username=qt_webengine_team\",\"name\":\"qt_webengine_team\",\"key\":\"qt_webengine_team\",\"avatarUrls\":{\"48x48\":\"https://bugreports.qt.io/secure/useravatar?ownerId=qt_webengine_team&avatarId=13712\",\"24x24\":\"https://bugreports.qt.io/secure/useravatar?size=small&ownerId=qt_webengine_team&avatarId=13712\",\"16x16\":\"https://bugreports.qt.io/secure/useravatar?size=xsmall&ownerId=qt_webengine_team&avatarId=13712\",\"32x32\":\"https://bugreports.qt.io/secure/useravatar?size=medium&ownerId=qt_webengine_team&avatarId=13712\"},\"displayName\":\"Qt WebEngine Team\",\"active\":true,\"timeZone\":\"Europe/Berlin\"},\"updated\":\"2017-11-06T12:04:51.000+0000\",\"status\":{\"self\":\"https://bugreports.qt.io/rest/api/2/status/10011\",\"description\":\"The issue has been reported, but no validation has been done on it.\",\"iconUrl\":\"https://bugreports.qt.io/images/icons/statuses/email.png\",\"name\":\"Reported\",\"id\":\"10011\",\"statusCategory\":{\"self\":\"https://bugreports.qt.io/rest/api/2/statuscategory/2\",\"id\":2,\"key\":\"new\",\"colorName\":\"blue-gray\",\"name\":\"To Do\"}},\"components\":[{\"self\":\"https://bugreports.qt.io/rest/api/2/component/22527\",\"id\":\"22527\",\"name\":\"General\"}],\"timeoriginalestimate\":null,\"description\":\"This is a general usability problem in QtQuick apps, which doesn't have a good solution yet, but I think we should come up with one.  We just need a way to know when a keyboard is hotplugged.\",\"timetracking\":{},\"customfield_10600\":null,\"customfield_10601\":null,\"customfield_10800\":\"0|i0gra8:\",\"attachment\":[],\"aggregatetimeestimate\":null,\"summary\":\"don't use the virtual keyboard if a real keyboard is attached\",\"creator\":{\"self\":\"https://bugreports.qt.io/rest/api/2/user?username=srutledg\",\"name\":\"srutledg\",\"key\":\"srutledg\",\"avatarUrls\":{\"48x48\":\"https://bugreports.qt.io/secure/useravatar?ownerId=srutledg&avatarId=12270\",\"24x24\":\"https://bugreports.qt.io/secure/useravatar?size=small&ownerId=srutledg&avatarId=12270\",\"16x16\":\"https://bugreports.qt.io/secure/useravatar?size=xsmall&ownerId=srutledg&avatarId=12270\",\"32x32\":\"https://bugreports.qt.io/secure/useravatar?size=medium&ownerId=srutledg&avatarId=12270\"},\"displayName\":\"Shawn Rutledge\",\"active\":true,\"timeZone\":\"Europe/Oslo\"},\"subtasks\":[],\"reporter\":{\"self\":\"https://bugreports.qt.io/rest/api/2/user?username=srutledg\",\"name\":\"srutledg\",\"key\":\"srutledg\",\"avatarUrls\":{\"48x48\":\"https://bugreports.qt.io/secure/useravatar?ownerId=srutledg&avatarId=12270\",\"24x24\":\"https://bugreports.qt.io/secure/useravatar?size=small&ownerId=srutledg&avatarId=12270\",\"16x16\":\"https://bugreports.qt.io/secure/useravatar?size=xsmall&ownerId=srutledg&avatarId=12270\",\"32x32\":\"https://bugreports.qt.io/secure/useravatar?size=medium&ownerId=srutledg&avatarId=12270\"},\"displayName\":\"Shawn Rutledge\",\"active\":true,\"timeZone\":\"Europe/Oslo\"},\"aggregateprogress\":{\"progress\":0,\"total\":0},\"customfield_10200\":\"9223372036854775807\",\"customfield_10400\":null,\"environment\":null,\"customfield_10910\":null,\"customfield_10911\":null,\"progress\":{\"progress\":0,\"total\":0},\"comment\":{\"comments\":[],\"maxResults\":0,\"total\":0,\"startAt\":0},\"votes\":{\"self\":\"https://bugreports.qt.io/rest/api/2/issue/QTWB-4/votes\",\"votes\":0,\"hasVoted\":false},\"worklog\":{\"startAt\":0,\"maxResults\":20,\"total\":0,\"worklogs\":[]}}}";
    	JsonObject issueElement2 = issueJSON.fromJson(jsonString2, JsonElement.class).getAsJsonObject();
    	
    	String dirPath = System.getProperty("user.dir") + "/src/test/resources/";
		String jsonString3 = new String(Files.readAllBytes(Paths.get(dirPath.toString() + 
				"fix_versions_issue.json"))); 
		String jsonString4 = new String(Files.readAllBytes(Paths.get(dirPath.toString() + 
				"fix_versions_issue_2.json"))); 
    	JsonObject issueElement3 = issueJSON.fromJson(jsonString3, JsonElement.class).getAsJsonObject();
    	JsonObject issueElement4 = issueJSON.fromJson(jsonString4, JsonElement.class).getAsJsonObject();
    	testElements.add(issueElement);
    	testElements.add(issueElement2);
    	testElements.add(issueElement3);
    	testElements.add(issueElement4);
    	
    }
    
    @After
    public void tearDown() {
    }
    
    @Test
    public void simpleTest() {
    	assertEquals(true, true);
    }
    
    @Test
    public void creatingAProjectWorks() {
    	String projectId = "TestProject1";
    	List<String> reqIds = new ArrayList<String>();
    	reqIds.add("REQ1");
    	reqIds.add("REQ2");
    	reqIds.add("REQ3");
    	Project project = transformerService.createProject(projectId, reqIds);
    	assertEquals(project.getId(), "TestProject1");
    	assertEquals(project.getSpecifiedRequirements().size(), 3);
    	assertEquals(project.getName(), "TestProject1");
    }
    
    @Test
    public void convertJsonElementsToIssuesWorks() throws IOException {
    	List<Issue> issues = transformerService.convertJsonElementsToIssues(testElements);

    	assertEquals(issues.size(), testElements.size());
    }
    
    @Test
    public void convertingIssuesToJsonWorks() throws Exception {
    	List<Issue> issues = transformerService.convertJsonElementsToIssues(testElements);
    	Collection<Requirement> reqCollection = transformerService.convertIssuesToJson(issues, projectId, person);
    	List<Requirement> reqList = new ArrayList<Requirement>(reqCollection);
    	Requirement testReq = reqList.get(0);
    	assertEquals(testReq.getId(), "QTWB-3");
    	assertEquals(testReq.getName(), "add shortcuts for zooming");
    	assertEquals(testReq.getText(), "Pinch-zooming on a touchscreen works, but I was expecting Ctrl-+/- and Ctrl-mousewheel to work too.");
    	assertEquals(testReq.getComments().isEmpty(), true);
    	assertEquals(testReq.getRequirement_type(), Requirement_type.ISSUE);
    	assertEquals(testReq.getStatus(), Requirement_status.SUBMITTED);
    	assertEquals(testReq.getPriority(), 3);
    	assertEquals(testReq.getRequirementParts().get(0).getText(), "Reported");
    }
    
    @Test
    public void jsonGetsCorrectFixVersion() throws Exception {    
    	List<Issue> issues = transformerService.convertJsonElementsToIssues(testElements);
    	Collection<Requirement> reqCollection = transformerService.convertIssuesToJson(issues, projectId, person);
    	List<Requirement> reqList = new ArrayList<Requirement>(reqCollection);
    	for (Requirement req : reqList) {
	    	for (RequirementPart reqp : req.getRequirementParts()) {
	    		System.out.println(reqp.getName());
	    		System.out.println(reqp.getText());
	    	}
    	}
//    	assertEquals(reqList.get(2).getRequirementParts().get(6).getText(), "4.9.0 Beta 1"); 	
//    	assertEquals(reqList.get(3).getRequirementParts().get(6).getText(), "4.9.0 Beta 1"); 	
    	assertEquals(reqList.get(2).getRequirementParts().get(6).getText(), "Qt Creator 4.9.0-beta1"); 	
    	assertEquals(reqList.get(3).getRequirementParts().get(6).getText(), "Qt Creator 4.9.0 (4.9 branch)"); 	

    }
    
}
