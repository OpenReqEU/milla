package eu.openreq.milla.services;

import org.apache.maven.artifact.versioning.ComparableVersion;
import org.springframework.stereotype.Service;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class FileService {
	
	
	public Map<String, Integer> readFixVersionsFromFile(String projectId) {
		List<ComparableVersion> fixVersions = new ArrayList<ComparableVersion>();
		Map<String, Integer> versionsMap = new HashMap<String, Integer>();
		try (BufferedReader reader = new BufferedReader(new FileReader("FixVersions/"+projectId+".txt"))) {
		    String line;
		    while ((line = reader.readLine()) != null) {
		       fixVersions.add(new ComparableVersion(line));
		    }
			Collections.sort(fixVersions);

			for (int i = fixVersions.size()-1; i >=0; i--) {
				versionsMap.put(fixVersions.get(i).toString(), i);
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			System.out.println("File not found, no fixVersions added");
//			e.printStackTrace();
			return null;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		
		return versionsMap;
	}

}
