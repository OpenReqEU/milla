package eu.openreq.milla.services;

import org.springframework.stereotype.Service;

import eu.openreq.milla.models.json.Dependency;
import eu.openreq.milla.models.json.Dependency_status;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class FileService {
	
	
	public Map<String, Integer> readFixVersionsFromFile(String projectId) {
		List<String> fixVersions = new ArrayList<String>();
		Map<String, Integer> versionsMap = new HashMap<String, Integer>();
		try (BufferedReader reader = new BufferedReader(new FileReader("FixVersions/"+projectId+".txt"))) {
		    String line;
		    while ((line = reader.readLine()) != null) {
		       fixVersions.add(line);
		    }
			Collections.reverse(fixVersions);

			for (int i = fixVersions.size()-1; i >=0; i--) {
				versionsMap.put(fixVersions.get(i), i);
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
	
	public void logDependencies(Collection<Dependency> dependencies) {
		try (BufferedWriter writer = new BufferedWriter(new FileWriter("logs/proposedDependencyUpdates.log", true))) {
			for (Dependency dep : dependencies) {
				if (dep.getStatus()!=null && dep.getStatus().equals(Dependency_status.ACCEPTED)) {
					writer.append("ACCEPTED " + dep.getId() + " " + dep.getDependency_type() + "\n");
				} 
				if (dep.getStatus()!=null && dep.getStatus().equals(Dependency_status.REJECTED)) {
					writer.append("REJECTED " + dep.getId() + " " + dep.getDependency_type() + "\n");
				}
			}
			writer.close();
		    
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			System.out.println("Log file not found");
			//e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

		}

	}

}
