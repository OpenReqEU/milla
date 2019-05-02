package eu.openreq.milla.qtjiraimporter;

import java.io.IOException;

import eu.openreq.milla.services.OAuthService;

/**
 * gets the Number of Issues from the website by fetching the newest issue and it's id
 */
public class NumberOfIssuesHTML
{
    private int _numberOfIssues;
    private String _URL;
    private String _project;
    private OAuthService authService;

    public NumberOfIssuesHTML(String project, OAuthService service) throws IOException
    {
    	authService = service;
        _URL = "/projects/"+ project +"/issues/?filter=allissues"; //A better way?: _URL = "/rest/api/2/search?jql=project=" + project + "&orderBy=-created&maxResults=1";
        _project = project;
        _numberOfIssues = detectNumberOfIssues();
        
    }

    private int detectNumberOfIssues() throws IOException
    {
        int projectStringlength = _project.length();
        String page = authService.authorizedJiraRequest(_URL);

        if(page!=null && page.contains("\\\"issueKeys\\\":"))
        {
            String text = (page.substring(page.indexOf("\\\"issueKeys\\\":")+17+projectStringlength, page.indexOf("\\\"issueKeys\\\":")+26+projectStringlength));
            int numberOfIssues = Integer.parseInt(text.replaceAll("[^0-9.]", ""));
            return numberOfIssues;
        }
        
        return -1;
    }

    public int getNumberOfIssues()
    {
        return _numberOfIssues;
    }
}
