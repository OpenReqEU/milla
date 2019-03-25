package eu.openreq.milla.qtjiraimporter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

public class ImportHTML
{
    public static ArrayList<String> importPage(String urlString, String credentials)
    {
        ArrayList<String> page = new ArrayList<String>();
        try
        {	
            URL url = new URL(urlString);
            URLConnection connection = url.openConnection();
            if (credentials!=null) {
            	connection.setRequestProperty("Authorization", credentials);
            }
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            while((inputLine = in.readLine()) != null)
            {
                page.add(inputLine);
            }
            in.close();
        }

        catch (MalformedURLException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return page;
    }
}
