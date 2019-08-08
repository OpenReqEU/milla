package eu.openreq.milla.services.test;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit.WireMockRule;

import eu.openreq.milla.models.json.Requirement;
import eu.openreq.milla.services.DetectionService;
import eu.openreq.milla.services.MallikasService;
import eu.openreq.milla.services.OAuthService;
import eu.openreq.milla.services.UpdateService;

@RunWith(SpringJUnit4ClassRunner.class)
public class UpdateServiceTest {
	
	@Rule
	public WireMockRule wm = new WireMockRule(WireMockConfiguration.wireMockConfig().port(8091), false);
	private String JIRA_BASE_URL = "http://localhost:8091";
	
	@Mock
	MallikasService mallikasService = new MallikasService();	
	
	@Mock
	DetectionService detectionService = new DetectionService();
	
	@InjectMocks
	private UpdateService updateService = new UpdateService();

	private static OAuthService authService = new OAuthService();
	
	private ObjectMapper mapper;
	
	@Before
    public void setUp() throws Exception {
		mapper = new ObjectMapper();
		
    	authService.setJiraBaseUrl(JIRA_BASE_URL);
    	
    	Mockito.when(mallikasService.updateRequirements(new ArrayList<Requirement>(), "TEST"))
			.thenReturn("some response");
	
		Mockito.when(mallikasService.getListOfProjects())
			.thenReturn("TEST");
		
		Requirement req = new Requirement();
		req.setName("QTWB-35");
		req.setId("265267");
		
		Mockito.when(mallikasService.getSelectedRequirements(new ArrayList<String>()))
			.thenReturn(mapper.writeValueAsString(req));
		
		Mockito.when(detectionService.postUpdatesToService(Mockito.anyString()))
			.thenReturn("Detection successful (big lie)");
		
	}
	
	@Test
	public void updateIssuesTest() throws Exception {
		String newestIssueUrl = "/rest/api/2/search?jql=project%3DTEST%20order%20by%20updated%20DESC&maxResults=1&startAt=0";
		String projectIssuesUrl = "/rest/api/2/search?jql=project%3DTEST%20order%20by%20updated%20DESC&maxResults=1000&startAt=0";
		
		String dirPath = System.getProperty("user.dir") + "/src/test/resources/";
		String jsonString = new String(Files.readAllBytes(Paths.get(dirPath.toString() + 
				"issueForUpdate.json")));
		
		stubFor(get(urlEqualTo(newestIssueUrl))
				.willReturn(aResponse().withStatus(200)
			    .withHeader("Content-Type", "text/html").withBody(jsonString)));


		stubFor(get(urlEqualTo(projectIssuesUrl))
				.willReturn(aResponse().withStatus(200)
			    .withHeader("Content-Type", "text/html").withBody(jsonString)));

		
		ResponseEntity<String> response = updateService.getAllUpdatedIssues("TEST", authService);
		System.out.println("Body: " + response.getBody().toString());
		assertEquals(response.getBody().length(), 1981);
	}
}
