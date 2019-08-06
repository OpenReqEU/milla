package eu.openreq.milla.services.test;

import org.apache.http.client.ClientProtocolException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.Assert.assertEquals;

import com.github.tomakehurst.wiremock.junit.WireMockRule;


import eu.openreq.milla.services.OAuthService;

import java.io.IOException;

@RunWith(SpringJUnit4ClassRunner.class)
public class OAuthServiceTest {	
	
	@Rule
	public WireMockRule wm = new WireMockRule(8089);
	
	private static final String REQUEST_TOKEN_URL = "/plugins/servlet/oauth/request-token";
	private static final String ACCESS_TOKEN_URL = "/plugins/servlet/oauth/access-token";
	private static final String AUTHORIZATION_URL = "/plugins/servlet/oauth/authorize";

	private String JIRA_BASE_URL = "http://localhost:8089";
	
	private OAuthService authService = new OAuthService();
	
	String responseJson = "{\"token\":\"Success\"}";
    
    @Before
    public void setUp() throws IOException {    	
    	authService.setJiraBaseUrl(JIRA_BASE_URL);   	
    }
    
    @Test
    public void testTempTokenAuthorization() throws ClientProtocolException, IOException {	
    	stubFor(post(urlEqualTo(REQUEST_TOKEN_URL))
    			.willReturn(aResponse().withStatus(200)
    		    .withHeader("Content-Type", "application/json").withBody(responseJson)));
    	String response = authService.tempTokenAuthorization();
    	verify(postRequestedFor(urlEqualTo(REQUEST_TOKEN_URL)));
    	assertEquals(response, "http://localhost:8089/plugins/servlet/oauth/authorize?oauth_token=null");
    }
    
    @Test
    public void testAccessTokenAuthorization() throws IOException {   	
    	String secret = "secret";
    	stubFor(post(urlEqualTo(ACCESS_TOKEN_URL))
    			.willReturn(aResponse().withStatus(200)
    		    .withHeader("Content-Type", "application/json").withBody(responseJson)));
    	String response = authService.accessTokenAuthorization(secret);
    	verify(postRequestedFor(urlEqualTo(ACCESS_TOKEN_URL)));
    	assertEquals(response, "Jira authorization complete");
    }
    
    @Test
    public void testAuthorizedRequest() {
    	stubFor(get(urlEqualTo(AUTHORIZATION_URL))
    			.willReturn(aResponse().withStatus(200)
    		    .withHeader("Content-Type", "application/json").withBody(responseJson)));
    	String response = authService.authorizedJiraRequest(AUTHORIZATION_URL);
    	verify(getRequestedFor(urlEqualTo(AUTHORIZATION_URL)));
    	assertEquals(response, responseJson);
    }
    
}
