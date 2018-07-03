package eu.openreq.milla.qtjiraimporter;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;

public class Run {
	
	/**
	 * Creates the connection to a requested url, and returns a response (Jira issue). If an issue requested is missing (404) or unauthorized (401), return null.
	 * @param url
	 * @param client
	 * @return
	 * @throws IOException
	 */
    public String run(String url, OkHttpClient client) throws IOException
    {
        Request request = new Request.Builder()
                .url(url)
                .build();

        try (Response response = client.newCall(request).execute())
        {	
        	if (!response.isSuccessful()) {
        		//System.out.println(response);
        		return null;
        	}
        	return response.body().string();
        }
        catch(Exception e) {
        	System.out.println(url);
        	System.out.println(e);
        	return null;
        }
    }
}