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

import eu.openreq.milla.MillaApplication;
import eu.openreq.milla.controllers.QtController;
import eu.openreq.milla.models.json.Dependency;
import eu.openreq.milla.services.ImportService;
import eu.openreq.milla.services.OAuthService;
import eu.openreq.milla.services.UpdateService;

import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Arrays;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes=MillaApplication.class)
@SpringBootTest
@AutoConfigureWebClient
public class QtControllerTest {

	@Value("${milla.mulperiAddress}")
	private String mulperiAddress;
	
	@Value("${milla.mallikasAddress}")
	private String mallikasAddress;
	
	@Value("${milla.upcSimilarityAddress}")
	private String upcSimilarityAddress;
	
	@Autowired
	QtController controller;

	@Autowired
	private RestTemplate rt;
	
	@Autowired
	private OAuthService authService;
	
	@MockBean
	private ImportService importService;

	@MockBean
	private UpdateService updateService;
	
	private MockMvc mockMvc;
	
	private MockRestServiceServer mockServer;
	
	@Before
	public void setup() throws Exception {
		mockServer = MockRestServiceServer.createServer(rt);
		mockMvc = MockMvcBuilders.standaloneSetup(controller).build();	
		
		Mockito.when(importService.importProjectIssues("testId", authService))
				.thenReturn(new ResponseEntity<String>("Success", HttpStatus.OK));
		
		Mockito.when(importService.importUpdatedIssues("testId", authService))
				.thenReturn(new ResponseEntity<String>("Success", HttpStatus.OK));
	}
	
	@Test
	public void transitiveClosureTest() throws Exception {
		
		mockServer.expect(requestTo(mulperiAddress + "/models/findTransitiveClosureOfRequirement?layerCount=4"))
				.andRespond(withSuccess("{\"dummy\":\"test\"}", MediaType.APPLICATION_JSON));
		
		mockMvc.perform(get("/getTransitiveClosureOfRequirement")
				.param("requirementId", "testId")
				.param("layerCount", "4"))
				.andExpect(status().isOk());	
		mockServer.verify();
	}
	
	@Test
	public void dependenciesOfRequirementTest() throws Exception {
		mockServer.expect(requestTo(mallikasAddress + "/dependenciesByParams"))
				.andRespond(withSuccess("{\"dummy\":\"test\"}", MediaType.APPLICATION_JSON));
		
		mockMvc.perform(get("/getDependenciesOfRequirement")
				.param("requirementId", "testId")
				.param("scoreThreshold", "0.4"))
				.andExpect(status().isOk());	
		mockServer.verify();
	}
	
	@Test
	public void consistencyCheckForRequirementTest() throws Exception {
		mockServer.expect(requestTo(mulperiAddress + "/models/consistencyCheckForTransitiveClosure?analysisOnly=false&timeOut=0"))
				.andRespond(withSuccess("{\"dummy\":\"test\"}", MediaType.APPLICATION_JSON));
		
		mockMvc.perform(get("/getConsistencyCheckForRequirement")
				.param("requirementId", "testId"))
				.andExpect(status().isOk());
		mockServer.verify();
	}
	
	@Test
	public void proposedDependencyTest() throws Exception {
		mockServer.expect(requestTo(mallikasAddress + "/dependenciesByParams"))
				.andRespond(withSuccess("{\"dummy\":\"test\"}", MediaType.APPLICATION_JSON));

		mockMvc.perform(get("/getProposedDependenciesOfRequirement")
				.param("requirementId", "testId"))
				.andExpect(status().isOk());
		mockServer.verify();
	}
	
	@Test
	public void topProposedDependencyTest() throws Exception {
		mockServer.expect(requestTo(mallikasAddress + "/dependenciesByParams"))
				.andExpect(method(HttpMethod.POST))
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
				.andRespond(withSuccess("{\"dummy\":\"test\"}", MediaType.APPLICATION_JSON_UTF8));

		mockMvc.perform(get("/getProposedDependenciesOfRequirement")
				.param("requirementId", "testId"))
				.andExpect(status().isOk());
		mockServer.verify();
	}
	
	
	@Test
	public void updateProjectTest() throws Exception {
		mockServer.expect(requestTo(mallikasAddress + "/projectRequirements?projectId=testId&includeProposed=false"
				+ "&requirementsOnly=false"))
				.andRespond(withSuccess("{\"dummy\":\"test\"}", MediaType.APPLICATION_JSON));
		
		mockServer.expect(requestTo(mulperiAddress + "/models/requirementsToChoco"))
				.andExpect(method(HttpMethod.POST))
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andRespond(withSuccess("Dummy success", MediaType.TEXT_PLAIN));
		
		mockMvc.perform(post("/updateProject")
				.param("projectId", "testId"))
				.andExpect(status().isOk());
		mockServer.verify();
	}
	
	@Test
	public void updateRecentInProjectTest() throws Exception {
		
		mockServer.expect(requestTo(mulperiAddress + "/models/updateModel"))
				.andExpect(method(HttpMethod.POST))
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andRespond(withSuccess("Dummy success", MediaType.TEXT_PLAIN));
		
		mockMvc.perform(post("/updateRecentInProject")
				.param("projectId", "testId"))
				.andExpect(status().isOk());
		mockServer.verify();
	}

	@Test
	public void updateProposedTest() throws Exception {	
		Dependency dep = new Dependency();
		dep.setId("test");
		ObjectMapper mapper = new ObjectMapper();
		String content = mapper.writeValueAsString(Arrays.asList(dep));

		mockServer.expect(requestTo(mallikasAddress + "/updateDependencies?userInput=true"))
				.andExpect(method(HttpMethod.POST))
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
				.andRespond(withSuccess("Dummy success", MediaType.TEXT_PLAIN));
		
		mockServer.expect(requestTo(upcSimilarityAddress + "/upc/similarity-detection/TreatAcceptedAndRejectedDependencies?organization=Qt"))
				.andExpect(method(HttpMethod.POST))
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
				.andRespond(withSuccess("Dummy success", MediaType.TEXT_PLAIN));
		
		mockMvc.perform(post("/updateProposedDependencies")
				.content(content))
				.andExpect(status().isOk());
		mockServer.verify();
	}
  
}
