package eu.openreq.milla.controllers.test;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.client.AutoConfigureWebClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;

import eu.openreq.milla.MillaApplication;
import eu.openreq.milla.controllers.MillaController;
import eu.openreq.milla.models.json.RequestParams;
import eu.openreq.milla.services.ImportService;
import eu.openreq.milla.services.MallikasService;
import eu.openreq.milla.services.MulperiService;
import eu.openreq.milla.services.OAuthService;

import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes=MillaApplication.class)
@SpringBootTest
@AutoConfigureWebClient
public class MillaControllerTest {
	
	@Value("${milla.mallikasAddress}")
	private String mallikasAddress;
	
	@Value("${milla.mulperiAddress}")
	private String mulperiAddress;
	
	@Value("${milla.jiraAddress}")
	private String JIRA_BASE_URL;
	
	@MockBean
	ImportService importService;
	
	@MockBean
	OAuthService authService;
	
	@Autowired
	MallikasService mallikasService;
	
	@Autowired
	MulperiService mulperiService;

	@Autowired
	MillaController controller;

	@Autowired
	private RestTemplate rt;

	private MockMvc mockMvc;
	
	private MockRestServiceServer mockServer;
	
	@Autowired
	private ObjectMapper mapper;
	
	@Before
	public void setup() throws Exception {
		mockServer = MockRestServiceServer.createServer(rt);
		mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
		mapper = new ObjectMapper();
		
		Mockito.when(importService.importProjectIssues("test", new OAuthService()))
			.thenReturn(new ResponseEntity<String>("test", HttpStatus.OK));
		
		Mockito.when(authService.tempTokenAuthorization())
			.thenReturn("testUrl");
		
		Mockito.when(authService.accessTokenAuthorization("testSecret"))
			.thenReturn("testUrl");
		
		Mockito.when(authService.isInitialized())
			.thenReturn(true);
		
		Mockito.when(authService.authorizedJiraRequest("/rest/auth/latest/session"))
			.thenReturn("test");
	}
	
	@Test
	public void sendProjectToMulperiTest() throws Exception {
		mockServer.expect(requestTo(mallikasAddress + "/projectRequirements?projectId=test&includeProposed=false"
				+ "&requirementsOnly=false"))
				.andRespond(withSuccess("{\"dummy\":\"test\"}", MediaType.APPLICATION_JSON));
		
		mockServer.expect(requestTo(mulperiAddress + "/models/requirementsToChoco"))
				.andExpect(method(HttpMethod.POST))
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andRespond(withSuccess("Dummy success", MediaType.TEXT_PLAIN));
		
		mockMvc.perform(post("/sendProjectToMulperi")
				.param("projectId", "test"))
				.andExpect(status().isOk());	
		mockServer.verify();
	}
	
	@Test
	public void requirementsByParamsTest() throws Exception {
		
		mockServer.expect(requestTo(mallikasAddress + "/requirementsByParams"))
				.andRespond(withSuccess("{\"dummy\":\"test\"}", MediaType.APPLICATION_JSON));
	
		RequestParams params = new RequestParams();
		params.setProjectId("test");
		
	    mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
	    ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
	    String requestJson = ow.writeValueAsString(params);
	    
		mockMvc.perform(post("/requirementsByParams")
				.contentType(MediaType.APPLICATION_JSON)
				.content(requestJson))
				.andExpect(status().isOk());
	
		mockServer.verify();
	}	
	
	@Test
	public void getRequirementsByIdsTest() throws Exception {		
		mockServer.expect(requestTo(mallikasAddress + "/selectedReqs"))
			.andRespond(withSuccess("Dummy success", MediaType.APPLICATION_JSON));
		
		mockMvc.perform(post("/requirementsByIds")
				.param("ids", "test"))
				.andExpect(status().isOk());	
		mockServer.verify();
	}
	
	@Test
	public void requirementsInProjectTest() throws Exception { 
		mockServer.expect(requestTo(mallikasAddress + "/projectRequirements?projectId=test&includeProposed=false"
				+ "&requirementsOnly=true")).andRespond(withSuccess("{\"dummy\":\"test\"}", 
						MediaType.APPLICATION_JSON));
		mockMvc.perform(get("/requirementsInProject")
				.param("projectId","test"))
				.andExpect(status().isOk());
		mockServer.verify();
	}
	
	@Test
	public void allRequirementsTest() throws Exception {
		mockServer.expect(requestTo(mallikasAddress + "/allRequirements"))
				.andRespond(withSuccess("{\"dummy\":\"test\"}", MediaType.APPLICATION_JSON));
		mockMvc.perform(get("/allRequirements"))
				.andExpect(status().isOk());
		mockServer.verify();
	}
	
	@Test
	public void qtJiraTest() throws Exception {
		mockMvc.perform(post("/qtJira")
				.param("projectId", "test"))
				.andExpect(status().isOk());
		
	}
	
	@Test
	public void qtJiraUpdateTest() throws Exception {
		mockServer.expect(requestTo(mallikasAddress + "/listOfProjects"))
				.andRespond(withSuccess("{\"dummy\":\"test\"}", MediaType.APPLICATION_JSON));
		mockMvc.perform(post("/qtJiraUpdated")
				.param("projectId", "test"))
				.andExpect(status().isOk());		
	}
	
	@Test
	public void jiraAuthorizationTest() throws Exception {
		mockMvc.perform(get("/getJiraAuthorizationAddress"))
				.andExpect(status().isOk());	
	}
	
	@Test
	public void jiraVerificationTest() throws Exception {
		mockServer.expect(requestTo(JIRA_BASE_URL + "/plugins/servlet/oauth/access-token"))
				.andRespond(withSuccess("Success", MediaType.TEXT_PLAIN));
		
		mockMvc.perform(post("/verifyJiraAuthorization")
				.contentType(MediaType.TEXT_PLAIN)
				.content("testSecret"))
				.andExpect(status().isOk());		
	}

	@Test
	public void jiraAuthorizationTestTest() throws Exception {
		mockMvc.perform(get("/testJiraAuthorization"))
				.andExpect(status().isOk());		
	}

  
}
