package eu.openreq.milla.services.test;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.client.AutoConfigureWebClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import eu.openreq.milla.MillaApplication;
import eu.openreq.milla.services.DetectionService;
import eu.openreq.milla.services.MallikasService;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes=MillaApplication.class)
@SpringBootTest
@AutoConfigureWebClient
public class DetectionServiceTest {

	@Value("${milla.detectionUpdateAddresses}")
	private String[] detectionUpdateAddresses;

	@Autowired
	private RestTemplate rt;
	
	@MockBean
	MallikasService mallikasService;

	@Autowired
	private DetectionService detectionService = new DetectionService();


	private MockRestServiceServer mockServer;
	
	@Before
    public void setUp() throws Exception {
		mockServer = MockRestServiceServer.createServer(rt);
		
		Mockito.when(mallikasService.convertAndUpdateDependencies(Matchers.any(), Matchers.anyBoolean(), Matchers.anyBoolean()))
			.thenReturn(new ResponseEntity<String>("Detection successful (supposedly)", HttpStatus.OK));
	
		
	}
	
	@Test
	public void updateIssuesTest() throws Exception {
		String dirPath = System.getProperty("user.dir") + "/src/test/resources/";
		String jsonString = new String(Files.readAllBytes(Paths.get(dirPath.toString() + 
				"issueForUpdate.json")));
		
		List<String> addresses = Arrays.asList(detectionUpdateAddresses);
		
		for (String url : addresses) {
			mockServer.expect(requestTo(url))
				.andRespond(withSuccess("{\"dummy\":\"test\"}", MediaType.APPLICATION_JSON));
		}
		
		String result = detectionService.postUpdatesToServices("TEST", jsonString);
		
		System.out.println(result);
		
		mockServer.verify();
		
		if (!addresses.isEmpty()) {
			assertTrue(result.contains(addresses.get(0)));
			assertTrue(result.contains("test"));
		}
	}
}
