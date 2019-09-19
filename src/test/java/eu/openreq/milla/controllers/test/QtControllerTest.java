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
import eu.openreq.milla.models.json.Dependency_status;
import eu.openreq.milla.models.json.Requirement;
import eu.openreq.milla.services.FileService;
import eu.openreq.milla.services.ImportService;
import eu.openreq.milla.services.OAuthService;
import eu.openreq.milla.services.UpdateService;

import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
	
	@Value("${milla.detectionGetAddresses}")
	private String[] detectionGetAddresses;
	
	@Value("${milla.detectionGetPostAddresses}")
	private String detectionGetPostAddresses;
	
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
	
	@MockBean
	FileService fs;
	
	private MockMvc mockMvc;
	
	private MockRestServiceServer mockServer;
	
	private List<Dependency> depsList;
	
	private String depsJson;
	
	private ObjectMapper mapper;
	
	@Before
	public void setup() throws Exception {
		mockServer = MockRestServiceServer.createServer(rt);
		mockMvc = MockMvcBuilders.standaloneSetup(controller).build();	
		
		Mockito.when(importService.importProjectIssues("testId", authService))
				.thenReturn(new ResponseEntity<String>("Success", HttpStatus.OK));
		
		Mockito.when(importService.importUpdatedIssues(Arrays.asList("testId"), authService))
				.thenReturn(new ResponseEntity<String>("Success", HttpStatus.OK));
		
		Mockito.when(fs.logDependencies(new ArrayList<Dependency>()))
				.thenReturn("Success");
		
		Dependency dep = new Dependency();
		dep.setId("test");
		dep.setStatus(Dependency_status.ACCEPTED);
		
		Dependency dep2 = new Dependency();
		dep2.setId("test2");
		dep2.setStatus(Dependency_status.REJECTED);
		
		mapper = new ObjectMapper();
		depsList = Arrays.asList(dep, dep2);
		depsJson = mapper.writeValueAsString(Arrays.asList(dep, dep2));
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
	public void transitiveClosureTestError() throws Exception {
		
		mockServer.expect(requestTo(mulperiAddress + "/models/findTransitiveClosureOfRequirement?layerCount=4"))
				.andRespond(withServerError());
		
		mockMvc.perform(get("/getTransitiveClosureOfRequirement")
				.param("requirementId", "testId")
				.param("layerCount", "4"))
				.andExpect(status().is5xxServerError());	
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
	public void consistencyCheckForRequirementTestError() throws Exception {
		mockServer.expect(requestTo(mulperiAddress + "/models/consistencyCheckForTransitiveClosure?analysisOnly=false"
				+ "&timeOut=0&omitCrossProject=false"))
				.andRespond(withServerError());
		
		mockMvc.perform(get("/getConsistencyCheckForRequirement")
				.param("requirementId", "testId"))
				.andExpect(status().is5xxServerError());
		mockServer.verify();
	}
	
	@Test
	public void consistencyCheckForRequirementTest() throws Exception {
		mockServer.expect(requestTo(mulperiAddress + "/models/consistencyCheckForTransitiveClosure?analysisOnly=false"
				+ "&timeOut=0&omitCrossProject=false"))
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
		Dependency dep = new Dependency();
		dep.setFromid("test");
		dep.setToid("test2");
		dep.setDependency_score(1);
		dep.setDescription(Arrays.asList("testDesc"));
		dep.setStatus(Dependency_status.PROPOSED);
		
		Dependency dep2 = new Dependency();
		dep2.setFromid("test3");
		dep2.setToid("test4");
		dep2.setDependency_score(1);
		dep2.setDescription(Arrays.asList("testDesc2"));
		dep2.setStatus(Dependency_status.ACCEPTED);
		
		Dependency dep3 = new Dependency();
		dep3.setFromid("test5");
		dep3.setToid("test6");
		dep3.setDependency_score(1);
		dep3.setDescription(Arrays.asList("testDesc3"));
		dep3.setStatus(Dependency_status.REJECTED);
		
		ObjectMapper mapper = new ObjectMapper();
		String content = mapper.writeValueAsString(Arrays.asList(dep, dep2, dep3));
		content = "{\"dependencies\":" + content + "}";
		
		Requirement req = new Requirement();
		req.setId("testReq");
		String reqContent = mapper.writeValueAsString(Arrays.asList(req));
		reqContent = "{\"requirements\":" + reqContent + "}";
		
		mockServer.expect(requestTo(mallikasAddress + "/dependenciesByParams"))
				.andRespond(withSuccess(content, MediaType.APPLICATION_JSON));
		
		for (String url : detectionGetAddresses) {
			mockServer.expect(requestTo(url + "testId"))
					.andExpect(method(HttpMethod.GET))
					.andRespond(withSuccess(content, MediaType.APPLICATION_JSON));
		}
		
		mockServer.expect(requestTo(detectionGetPostAddresses + "testId"))
			.andExpect(method(HttpMethod.POST))
			.andRespond(withSuccess(content, MediaType.APPLICATION_JSON));
		
		mockServer.expect(requestTo(mallikasAddress + "/selectedReqs"))
			.andRespond(withSuccess(reqContent, MediaType.APPLICATION_JSON));
		
		int score = 1 + 1 * detectionGetAddresses.length + (detectionGetPostAddresses!=null ? 1 : 0);
		
		mockMvc.perform(get("/getTopProposedDependenciesOfRequirement")
				.param("requirementId", "testId"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.requirements[0].id").value("testReq"))	
				.andExpect(jsonPath("$.dependencies[0].id").value("test_test2"))	
				.andExpect(jsonPath("$.dependencies[0].dependency_score").value(score))	
				.andExpect((jsonPath("$.dependencies[1].id").doesNotExist()));	
		mockServer.verify();
	}
	
	
	@Test
	public void updateProjectTest() throws Exception {
		mockServer.expect(requestTo(mallikasAddress + "/projectRequirements?projectId=testId&includeProposed=false"
				+ "&requirementsOnly=false"))
				.andRespond(withSuccess("{\"dummy\":\"test\"}", MediaType.APPLICATION_JSON));
		
		mockServer.expect(requestTo(mulperiAddress + "/models/murmeliModelToKeljuCaas"))
				.andExpect(method(HttpMethod.POST))
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andRespond(withSuccess("Dummy success", MediaType.TEXT_PLAIN));
		
		mockMvc.perform(post("/updateProject")
				.param("projectId", "testId"))
				.andExpect(status().isOk());
		mockServer.verify();
	}
	
	
	@Test
	public void updateProjectTestError() throws Exception {
		mockServer.expect(requestTo(mallikasAddress + "/projectRequirements?projectId=testId&includeProposed=false"
				+ "&requirementsOnly=false"))
				.andRespond(withServerError());
		
		mockMvc.perform(post("/updateProject")
				.param("projectId", "testId"))
				.andExpect(status().is5xxServerError());
		mockServer.verify();
	}
	
	@Test
	public void updateRecentInProjectTest() throws Exception {
		mockMvc.perform(post("/updateRecentInProject")
				.param("projectId", "testId"))
				.andExpect(status().isOk());
		mockServer.verify();
	}
	
	@Test
	public void updateRecentForAllProjectsTest() throws Exception {
		mockServer.expect(requestTo(mallikasAddress + "/listOfProjects"))
				.andExpect(method(HttpMethod.GET))
				.andRespond(withSuccess("{\"testId\":10}", MediaType.TEXT_PLAIN));
		
		mockMvc.perform(post("/updateRecentForAllProjects"))
				.andExpect(status().isOk());
		mockServer.verify();
	}

	@Test
	public void updateProposedTest() throws Exception {			
		Map<String, List<Dependency>> depsMap = new HashMap<>();
		depsMap.put("test", depsList);

		mockServer.expect(requestTo(mallikasAddress + "/updateDependencies?userInput=true"))
				.andExpect(method(HttpMethod.POST))
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
				.andRespond(withSuccess("Dummy success", MediaType.TEXT_PLAIN));
		
		mockServer.expect(requestTo(mallikasAddress + "/correctIdsForDependencies"))
				.andExpect(method(HttpMethod.POST))
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
				.andRespond(withSuccess(mapper.writeValueAsString(depsList), MediaType.APPLICATION_JSON));
		
		mockServer.expect(requestTo(upcSimilarityAddress + "/upc/similarity-detection/TreatAcceptedAndRejectedDependencies?organization=Qt"))
				.andExpect(method(HttpMethod.POST))
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
				.andRespond(withSuccess("Dummy success", MediaType.TEXT_PLAIN));
		

		mockServer.expect(requestTo(mallikasAddress + "/projectsForDependencies"))
				.andExpect(method(HttpMethod.POST))
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
				.andRespond(withSuccess(mapper.writeValueAsString(depsMap), MediaType.APPLICATION_JSON));
				
		mockServer.expect(requestTo(mulperiAddress + "/models/updateMurmeliModelInKeljuCaas"))
				.andExpect(method(HttpMethod.POST))
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andRespond(withSuccess("Dummy success", MediaType.TEXT_PLAIN));
			
		mockMvc.perform(post("/updateProposedDependencies")
				.content(depsJson)
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());
		mockServer.verify();
	}
	
	@Test
	public void updateProposedTestError() throws Exception {		
		mockServer.expect(requestTo(mallikasAddress + "/updateDependencies?userInput=true"))
				.andRespond(withServerError());
		
		mockMvc.perform(post("/updateProposedDependencies")
				.content(depsJson)
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().is5xxServerError());
		mockServer.verify();
	}
  
}
