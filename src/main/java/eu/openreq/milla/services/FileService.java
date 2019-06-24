package eu.openreq.milla.services;

import org.springframework.stereotype.Service;

import eu.openreq.milla.models.json.Dependency;
import eu.openreq.milla.models.json.Dependency_status;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;


@Service
public class FileService {
	
	public void logDependencies(Collection<Dependency> dependencies) {
		try (BufferedWriter writer = new BufferedWriter(new FileWriter("logs/proposedDependencyUpdates.log", true))) {
			for (Dependency dep : dependencies) {
				if (dep.getStatus()!=null && dep.getStatus().equals(Dependency_status.ACCEPTED)) {
					writer.append("ACCEPTED " + dep.getFromid() + "_" + dep.getToid() + " " + dep.getDependency_type() + "\n");
				} 
				if (dep.getStatus()!=null && dep.getStatus().equals(Dependency_status.REJECTED)) {
					writer.append("REJECTED " + dep.getFromid() + "_" + dep.getToid() + " " + dep.getDependency_type() + "\n");
				}
			}
			writer.close();
		    
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			System.out.println("Log file not found");
			//e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println(e.getMessage());

		}

	}

}
