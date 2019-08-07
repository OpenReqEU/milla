package eu.openreq.milla.services.test;

import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withBadRequest;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.client.AutoConfigureWebClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import eu.openreq.milla.MillaApplication;
import eu.openreq.milla.models.json.Requirement;
import eu.openreq.milla.services.MallikasService;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes=MillaApplication.class)
@SpringBootTest
@AutoConfigureWebClient
public class MallikasServiceTest {	

	@Value("${milla.mallikasAddress}")
	private String mallikasAddress;
	
	@Autowired
	private RestTemplate rt;
	
	@Autowired
	private MallikasService mallikasService;

	private MockRestServiceServer mockServer;
	
	String projectId = "TEST";
    
    @Before
    public void setUp() throws Exception {    	
    	mockServer = MockRestServiceServer.createServer(rt);
    }
    
    @Test
    public void testListOfProjects() throws Exception {
    	mockServer.expect(requestTo(mallikasAddress + "/listOfProjects"))
			.andRespond(withSuccess("{\"dummy\":\"test\"}", MediaType.APPLICATION_JSON));
    	
    	mallikasService.getListOfProjects();
    	
    	mockServer.verify();
    }
    
    @Test
    public void testAllRequirements() throws Exception {
    	mockServer.expect(requestTo(mallikasAddress + "/allRequirements"))
			.andRespond(withSuccess("{\"dummy\":\"test\"}", MediaType.APPLICATION_JSON));
    	
    	mallikasService.getAllRequirements();
    	
    	mockServer.verify();
    }
    
    @Test
    public void testAllRequirementsInProject() throws Exception {
    	
    	String url = mallikasAddress + "/projectRequirements?projectId=" + projectId + 
		"&includeProposed=true&requirementsOnly=true";
    	
    	mockServer.expect(requestTo(url))
			.andRespond(withSuccess("{\"dummy\":\"test\"}", MediaType.APPLICATION_JSON));
    	
    	mallikasService.getAllRequirementsInProject("TEST", true, true);
    	
    	mockServer.verify();
    }
    
    @Test
    public void testUpdateRequirements() throws Exception {
    	Requirement req = new Requirement();
    	req.setName("test");
    	
    	mockServer.expect(requestTo(mallikasAddress + "/updateRequirements?projectId=" + projectId))
    		.andExpect(content().string("[{\"id\":null,\"name\":\"test\",\"text\":null,\"comments\":[],\"created_at\":0,"
    				+ "\"modified_at\":0,\"priority\":0,\"requirement_type\":null,\"status\":null,"
    				+ "\"children\":[],\"requirementParts\":[]}]"))
			.andRespond(withSuccess("{\"dummy\":\"test\"}", MediaType.APPLICATION_JSON));
    	
    	mallikasService.updateRequirements(Arrays.asList(req), projectId);
    	
    	mockServer.verify();
    }
    
    @Test
    public void testUpdateReqIds() throws Exception {
    	List<String> reqIds = Arrays.asList("test");
    	
    	mockServer.expect(requestTo(mallikasAddress + "/updateProjectSpecifiedRequirements?projectId=" + projectId))
			.andRespond(withSuccess("{\"dummy\":\"test\"}", MediaType.APPLICATION_JSON));
    	
    	mallikasService.updateReqIds(reqIds, projectId);
    	mockServer.verify();
    }
    
    @Test
    public void testErrorResponse() throws Exception {
    	mockServer.expect(requestTo(mallikasAddress + "/allRequirements"))
			.andRespond(withBadRequest());
	
    	String response = mallikasService.getAllRequirements();
	
    	mockServer.verify();
    	
    	assertEquals("", response);
    }
    
    
}
