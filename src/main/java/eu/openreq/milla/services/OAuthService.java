package eu.openreq.milla.services;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Scanner;

import org.json.JSONObject;

import com.google.api.client.auth.oauth.OAuthCredentialsResponse;
import com.google.api.client.auth.oauth.OAuthParameters;
import com.google.api.client.auth.oauth.OAuthRsaSigner;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.apache.ApacheHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.repackaged.org.apache.commons.codec.binary.Base64;

public class OAuthService {

	private static final String JIRA_BASE_URL = "http://localhost:2990/jira";
	private static final String REQUEST_TOKEN_URL = "/plugins/servlet/oauth/request-token";
	private static final String ACCESS_TOKEN_URL = "/plugins/servlet/oauth/access-token";
	private static final String AUTHORIZATION_URL = "/plugins/servlet/oauth/authorize";
	
	private static final String CONSUMER_KEY = "OauthKey";
	private static String PRIVATE_KEY;
	private String REQUEST_TOKEN;
	private String ACCESS_TOKEN;
	private String SECRET;
	private OAuthParameters parameters;
	private OAuthRsaSigner signer;

	public OAuthService() throws InvalidKeySpecException, NoSuchAlgorithmException, IOException {
		byte[] encoded = Files.readAllBytes(Paths.get("key.txt"));
		PRIVATE_KEY = new String(encoded, StandardCharsets.UTF_8);
		signer = new OAuthRsaSigner();
	    signer.privateKey = getPrivate();
	}
	
	public String tempTokenAuthorization() throws InvalidKeySpecException, NoSuchAlgorithmException, IOException {;
		JiraOAuthGetTemporaryToken getTemp = new JiraOAuthGetTemporaryToken(JIRA_BASE_URL + REQUEST_TOKEN_URL);
		getTemp.consumerKey = CONSUMER_KEY;
		getTemp.callback = "oob";
		getTemp.signer = signer;
		getTemp.transport = new ApacheHttpTransport();
		
		
		OAuthCredentialsResponse response = getTemp.execute();
		
		System.out.println("Token:\t\t\t" + response.token);
	    System.out.println("Token secret:\t" + response.tokenSecret);

	    String authorizationURL = JIRA_BASE_URL + AUTHORIZATION_URL + "?oauth_token=" + response.token;
        
        REQUEST_TOKEN = response.token;

        return "Authorize token at " + authorizationURL;
       
	}
	
	public String accessTokenAuthorization(String secret) throws IOException {
		JiraOAuthGetAccessToken getAcc = new JiraOAuthGetAccessToken(JIRA_BASE_URL + ACCESS_TOKEN_URL);
		getAcc.consumerKey = CONSUMER_KEY;
	    getAcc.signer = signer;
	    getAcc.verifier = secret;
	    getAcc.temporaryToken = REQUEST_TOKEN;
	    getAcc.transport = new ApacheHttpTransport();
	    
	    OAuthCredentialsResponse response = getAcc.execute();
	    
	    ACCESS_TOKEN = response.token;
	    SECRET = response.tokenSecret;
	    
	    System.out.println("Token:\t\t\t" + response.token);
	    System.out.println("Token secret:\t" + response.tokenSecret);
	    
	    parameters = new OAuthParameters();
		parameters.consumerKey = CONSUMER_KEY;
	    parameters.signer = signer;
	    parameters.verifier = SECRET;
	    parameters.token = ACCESS_TOKEN;
	    	
	    return "Access token obtained: " + response.token;		
	    
	}
	
	private PrivateKey getPrivate() throws InvalidKeySpecException, NoSuchAlgorithmException {
		byte[] privateBytes = Base64.decodeBase64(PRIVATE_KEY);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privateBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePrivate(keySpec);
	}
	public String authorizedRequest(String url) throws IOException {
	    if (parameters==null) {
	    	return "Authorization not complete";
	    }
	    HttpRequestFactory requestFactory = new NetHttpTransport().createRequestFactory(parameters);
        HttpRequest request = requestFactory.buildGetRequest(new GenericUrl(url));
        HttpResponse response = request.execute();
        
        return  parseResponse(response);
        
	}
	
	
   

	/**
	* Prints response content
	* if response content is valid JSON it prints it in 'pretty' format
	*
	* @param response
	* @throws IOException
	*/
	private String parseResponse(HttpResponse response) throws IOException {
		Scanner s = new Scanner(response.getContent()).useDelimiter("\\A");
		String result = s.hasNext() ? s.next() : "";
		
		try {
		    JSONObject jsonObj = new JSONObject(result);
		    result += (jsonObj.toString(2));
		} catch (Exception e) {
		    System.out.println(result);
		}
		s.close();
		return result;
	}

}
