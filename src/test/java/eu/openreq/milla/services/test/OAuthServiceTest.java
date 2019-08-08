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
	
	String testKey = "MIICdgIBADANBgkqhkiG9w0BAQEFAASCAmAwggJcAgEAAoGBAJgZpnp0gZ+lJvG3" + 
			"m9oSGRQP93zrxobQxiDZGFxQ70x3wk394bSbkPk0Oun6DlKAZn1WNyRFu1YZPQnh" + 
			"k5ND/zM1TpVxnG4ha8GEYD43+W/Zwr8DLEhEeN06AaJxpX6neZHSXOn6HwJ+5Ayr" + 
			"KSQScQ/2RTj9dF46wR75hs7ghmwdAgMBAAECgYAaWl9g2izXV0sPGGv1datIsZeE" + 
			"2mkUVLnvWQ4CeLdtfVZ6IuHnZAjTVLxca8nte3fGgZiePUK/ITJVMvNZ0a82kMWa" + 
			"Xyc6+NoBhJHJikRDpx4g32bcDKwjEJvakZhe5UTZjed+pPyA36LbHJs4V6akOWz6" + 
			"0Kf43cVSFiSdsjlz4QJBAMZ3LIAChB1zSODt948DU6O7dDGI8ntpCasTiwloaKhE" + 
			"CUhuhaiQrr1zFmTb5Ukv1hgG3/NH0cYTnA/pvuTgB/UCQQDEMYrKHHKXkvb36od/" + 
			"o/rnPPjyEGAwcxk6gBb3/N3HCEoMmgnEN5lk95tz0BkvC+MXJJLDdGkn6EottN+s" + 
			"IUKJAkEAhhV73hxVD+SUZ0q+x0NTHbqGvPSuBkynuIoilD+S/aPBtcrdyE2/kMUR" + 
			"ayDZXsHP3jm+0glCo5UaCfI9AzqMhQJAaluRKdASLyl0ySFTI1b6BbGAI3nNK3a6" + 
			"DoSx7u4eLG/J9NrswHqDpcC1fSsq+94t1bX1+g95kjDTRcPwHZhKQQJAF+aWMHQc" + 
			"8Syn66ahwLCm209FJQ2rIkh8TPWWsREspLgeH0rVCVclqag3olFTqH+vjaG0sFA5" + 
			"sXhexmsE5DNTVA==";
	
	String badKey = "asdasdasd";
	
    @Before
    public void setUp() throws IOException {
    	authService.setJiraBaseUrl(JIRA_BASE_URL);   	
    }
    
    @Test
    public void testTempTokenAuthorizationNoKey() throws ClientProtocolException, IOException {	
    	authService.setPrivateKey(null);
    	String response = authService.tempTokenAuthorization();;
    	assertEquals(response, null);
    }
    
    @Test
    public void setBadKeyFirst() throws ClientProtocolException, IOException {	
    	authService.setPrivateKey(badKey);
    	authService.setPrivateKey(testKey);
    }
    
    @Test
    public void testTempTokenAuthorization() throws ClientProtocolException, IOException {	
    	authService.setPrivateKey(testKey);
    	stubFor(post(urlEqualTo(REQUEST_TOKEN_URL))
    			.willReturn(aResponse().withStatus(200)
    		    .withHeader("Content-Type", "application/json").withBody(responseJson)));
    	String response = authService.tempTokenAuthorization();
    	verify(postRequestedFor(urlEqualTo(REQUEST_TOKEN_URL)));
    	assertEquals(response, "http://localhost:8089/plugins/servlet/oauth/authorize?oauth_token=null");
    }
    
    @Test
    public void testAccessTokenAuthorizationNoKey() throws ClientProtocolException, IOException {	
    	authService.setPrivateKey(null);
    	String response = authService.accessTokenAuthorization("secret");;
    	assertEquals(response, null);
    }
    
    @Test
    public void testAccessTokenAuthorization() throws IOException {  
    	authService.setPrivateKey(testKey);
    	
    	String secret = "secret";
    	stubFor(post(urlEqualTo(ACCESS_TOKEN_URL))
    			.willReturn(aResponse().withStatus(200)
    		    .withHeader("Content-Type", "application/json").withBody(responseJson)));
    	String response = authService.accessTokenAuthorization(secret);
    	verify(postRequestedFor(urlEqualTo(ACCESS_TOKEN_URL)));
    	assertEquals(response, "Jira authorization complete");
    }
    
    @Test
    public void testAuthorizedRequestNoKey() throws ClientProtocolException, IOException {	
    	authService.setPrivateKey(null);
    	String response = authService.authorizedJiraRequest(AUTHORIZATION_URL);
    	assertEquals(response, null);
    }
    
    @Test
    public void testAuthorizedRequest() {
    	authService.setPrivateKey(testKey);
    	stubFor(get(urlEqualTo(AUTHORIZATION_URL))
    			.willReturn(aResponse().withStatus(200)
    		    .withHeader("Content-Type", "application/json").withBody(responseJson)));
    	String response = authService.authorizedJiraRequest(AUTHORIZATION_URL);
    	verify(getRequestedFor(urlEqualTo(AUTHORIZATION_URL)));
    	assertEquals(response, responseJson);
    }
    
}
