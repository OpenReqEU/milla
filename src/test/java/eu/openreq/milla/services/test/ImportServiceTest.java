package eu.openreq.milla.services.test;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.Assert.assertEquals;

import com.github.tomakehurst.wiremock.junit.WireMockRule;

import eu.openreq.milla.models.json.Requirement;
import eu.openreq.milla.services.FormatTransformerService;
import eu.openreq.milla.services.ImportService;
import eu.openreq.milla.services.MallikasService;
import eu.openreq.milla.services.OAuthService;
import eu.openreq.milla.services.UpdateService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;

@RunWith(SpringJUnit4ClassRunner.class)
public class ImportServiceTest {	
	
	@Rule
	public WireMockRule wm = new WireMockRule(8090);
	private String JIRA_BASE_URL = "http://localhost:8090";
	
	@Mock
	FormatTransformerService formatService = new FormatTransformerService();
	
	@Mock
	MallikasService mallikasService = new MallikasService();
	
	@Mock
	UpdateService updateService = new UpdateService();
	
	@InjectMocks
	private ImportService importService = new ImportService();
	
	private OAuthService authService = new OAuthService();
    
    @Before
    public void setUp() throws Exception {    
    	authService.setJiraBaseUrl(JIRA_BASE_URL); 
    	
    	Mockito.when(mallikasService.updateRequirements(new ArrayList<Requirement>(), "test"))
			.thenReturn("test");
    	
    	Mockito.when(mallikasService.getListOfProjects())
			.thenReturn("{\"TEST\":10}");
    	
    	Mockito.when(updateService.getAllUpdatedIssues(Arrays.asList("TEST"), authService))
			.thenReturn(new ResponseEntity<String>("Success", HttpStatus.OK));
    	
    }
    
    @Test
    public void testProjectImport() throws Exception {
    	String issueNumberUrl = "/projects/TEST/issues/?filter=allissues";
    	String fakeHtml = "\\\"issueKeys\\\":[\\\"TEST-1\\\"]\\\\\\\\";
    	
    	String issueUrl = "/rest/api/2/issue/TEST-1";
    	
		String dirPath = System.getProperty("user.dir") + "/src/test/resources/";
		String jsonString = new String(Files.readAllBytes(Paths.get(dirPath.toString() + 
				"dance.json")));
    	
    	stubFor(get(urlEqualTo(issueNumberUrl))
    			.willReturn(aResponse().withStatus(200)
    		    .withHeader("Content-Type", "text/html").withBody(fakeHtml)));
    	
    	stubFor(get(urlEqualTo(issueUrl))
    			.willReturn(aResponse().withStatus(200)
    		    .withHeader("Content-Type", "text/html").withBody(jsonString)));
    	
    	ResponseEntity<String> response = importService.importProjectIssues("TEST", authService);
    	
    	assertEquals(response.getBody(), "All requirements and dependencies downloaded");
    }
    
    @Test
    public void testUpdateImport() throws IOException {    	
    	ResponseEntity<String> response = importService.importUpdatedIssues(Arrays.asList("TEST"), authService);
    	
    	assertEquals(response.getBody(), "Success");
    }
    
}
