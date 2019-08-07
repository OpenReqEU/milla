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
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.openreq.milla.MillaApplication;
import eu.openreq.milla.controllers.DetectionController;
import eu.openreq.milla.models.json.Dependency;
import eu.openreq.milla.models.json.Dependency_status;
import eu.openreq.milla.services.FileService;
import eu.openreq.milla.services.MallikasService;

import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.Arrays;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes=MillaApplication.class)
@SpringBootTest
@AutoConfigureWebClient
public class DetectionControllerTest {
	
	@Value("${milla.upcSimilarityAddress}")
	private String upcSimilarityAddress;
	
	@Value("${milla.upcCrossReferenceAddress}")
	private String upcCrossReferenceAddress;
	
	@Value("${milla.detectionGetAddresses}")
	private String[] detectionGetAddresses;
	
	@Value("${milla.detectionGetPostAddress}")
	private String detectionGetPostAddress;
	
	@Value("${milla.detectionPostAddresses}")
	private String[] detectionPostAddresses;
	
	@Value("${milla.mallikasAddress}")
	private String mallikasAddress;
	
	@MockBean
	FileService fs;
	
	@Autowired
	MallikasService mallikasService;

	@Autowired
	DetectionController controller;	

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
		
		Mockito.when(fs.logDependencies(new ArrayList<Dependency>()))
			.thenReturn("Success");
	}
	
	@Test
	public void getDetectedTest() throws Exception {
		String testAddress = "http://localhost:9203/test?id=";
		String testId = "testId";
		
		mockServer.expect(requestTo(testAddress + testId))
				.andRespond(withSuccess("{\"dummy\":\"test\"}", MediaType.APPLICATION_JSON));
		
		mockMvc.perform(get("/detectedFromService")
				.param("requirementId", testId)
				.param("url", testAddress))
				.andExpect(status().isOk());	
		mockServer.verify();
	}
	
	@Test
	public void getDetectedsTest() throws Exception {
		String testId = "testId";
		
		for (String testAddress : detectionGetAddresses) {
			mockServer.expect(requestTo(testAddress + testId))
					.andRespond(withSuccess("{\"dummy\":\"test\"}", MediaType.APPLICATION_JSON));
		}
		
		mockServer.expect(requestTo(detectionGetPostAddress + "testId"))
			.andExpect(method(HttpMethod.POST))
			.andRespond(withSuccess("{\"dummy\":\"test\"}", MediaType.APPLICATION_JSON));
		
		mockMvc.perform(get("/detectedFromServices")
				.param("requirementId", testId))
				.andExpect(status().isOk());	
		mockServer.verify();
	}
	
	@Test
	public void postProjectTest() throws Exception {
		String testAddress = "http://localhost:9203/test";
		String testId = "testId";
		
		mockServer.expect(requestTo(mallikasAddress + "/projectRequirements?projectId=testId&includeProposed=true"
				+ "&requirementsOnly=false")).andRespond(withSuccess("{\"dummy\":\"test\"}", 
						MediaType.APPLICATION_JSON));
		
		mockServer.expect(requestTo(testAddress))
				.andExpect(content().string("{\"dummy\":\"test\"}"))
				.andRespond(withSuccess("[{\"response\":\"test\"}]", MediaType.APPLICATION_JSON));
		
		mockServer.expect(requestTo(mallikasAddress + "/updateDependencies?isProposed=true"))
				.andRespond(withSuccess("{\"dummy\":\"test\"}", 
						MediaType.APPLICATION_JSON));
		
		mockMvc.perform(post("/projectToService")
				.param("projectId", testId)
				.param("url", testAddress))
				.andExpect(status().isOk());	
		mockServer.verify();
	}
	

	@Test
	public void projectToServicesTest() throws Exception {
		String testId = "testId";
		
		mockServer.expect(requestTo(mallikasAddress + "/projectRequirements?projectId=testId&includeProposed=true"
				+ "&requirementsOnly=false")).andRespond(withSuccess("{\"dummy\":\"test\"}", 
						MediaType.APPLICATION_JSON));
		for (String testAddress : detectionPostAddresses) {
			mockServer.expect(requestTo(testAddress))
					.andExpect(content().string("{\"dummy\":\"test\"}"))
					.andRespond(withSuccess("[{\"response\":\"test\"}]", MediaType.APPLICATION_JSON));
			
			mockServer.expect(requestTo(mallikasAddress + "/updateDependencies?isProposed=true"))
					.andRespond(withSuccess("{\"dummy\":\"test\"}", 
							MediaType.APPLICATION_JSON));
		}
		
		mockMvc.perform(post("/projectToServices")
				.param("projectId", testId))
				.andExpect(status().isOk());	
		mockServer.verify();
	}
	
	@Test
	public void allProjectsToServicesTest() throws Exception {
		
		mockServer.expect(requestTo(mallikasAddress + "/allRequirements"))
				.andRespond(withSuccess("{\"dummy\":\"test\"}", 
						MediaType.APPLICATION_JSON));
		for (String testAddress : detectionPostAddresses) {
			mockServer.expect(requestTo(testAddress))
					.andExpect(content().string("{\"dummy\":\"test\"}"))
					.andRespond(withSuccess("[{\"response\":\"test\"}]", MediaType.APPLICATION_JSON));
			
			mockServer.expect(requestTo(mallikasAddress + "/updateDependencies?isProposed=true"))
					.andRespond(withSuccess("{\"dummy\":\"test\"}", 
							MediaType.APPLICATION_JSON));
		}
		
		mockMvc.perform(post("/projectToServices")
				.param("projectId", "ALL"))
				.andExpect(status().isOk());	
		mockServer.verify();
	}
	
	@Test
	public void projectToOrsiTest() throws Exception {
		String testId = "id";
		
		mockServer.expect(requestTo(mallikasAddress + "/projectRequirements?projectId=id&includeProposed=true"
				+ "&requirementsOnly=false")).andRespond(withSuccess("{\"dummy\":\"test\"}", 
						MediaType.APPLICATION_JSON));
		
		mockServer.expect(requestTo(upcSimilarityAddress + "/upc/similarity-detection/BuildClustersAndCompute"))
				.andRespond(withSuccess("{\"response\":\"test\"}", MediaType.APPLICATION_JSON_UTF8));
		
		mockMvc.perform(post("/projectToORSI")
				.param("threshold", "0.1")
				.param("projectId", testId))
				.andExpect(status().isOk());	
		mockServer.verify();
	}
	
	@Test
	public void addReqResponseTest() throws Exception {
		
		MockMultipartFile file = new MockMultipartFile("result", "", "application/json", 
				"{\"json\":\"test\"}".getBytes());
		
		mockMvc.perform(MockMvcRequestBuilders.fileUpload("/receiveAddReqResponse")
				.file(file))
				.andExpect(status().isOk());
	}
	
	@Test
	public void acceptedAndRejectedTest() throws Exception {
  
    	Dependency dep = new Dependency();
		dep.setId("test");
		dep.setStatus(Dependency_status.ACCEPTED);
		
		Dependency dep2 = new Dependency();
		dep.setId("test2");
		dep.setStatus(Dependency_status.REJECTED);
		
		String content = mapper.writeValueAsString(Arrays.asList(dep, dep2));
		
		mockServer.expect(requestTo(upcSimilarityAddress + "/upc/similarity-detection/TreatAcceptedAnd"
				+ "RejectedDependencies?organization=Qt")).andRespond(withSuccess("{\"response\":\"test\"}", 
						MediaType.APPLICATION_JSON));
		
		
		mockMvc.perform(post("/acceptedAndRejectedToORSI")
				.contentType(MediaType.APPLICATION_JSON)
				.content(content))
				.andExpect(status().isOk());	
		
		mockServer.verify();
	}

  
}
