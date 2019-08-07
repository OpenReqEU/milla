package eu.openreq.milla.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import eu.openreq.milla.models.json.Dependency;
import eu.openreq.milla.models.json.Dependency_status;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;


@Service
public class FileService {
	
	@Value("${milla.dependencyUpdateLogFile}")
	private String logFile;
	
	public String logDependencies(Collection<Dependency> dependencies) {
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(logFile, true))) {
			for (Dependency dep : dependencies) {
				if (dep.getStatus()!=null && dep.getStatus().equals(Dependency_status.ACCEPTED)) {
					writer.append("ACCEPTED " + dep.getFromid() + "_" + dep.getToid() + " " + dep.getDependency_type() + "\n");
				} 
				if (dep.getStatus()!=null && dep.getStatus().equals(Dependency_status.REJECTED)) {
					writer.append("REJECTED " + dep.getFromid() + "_" + dep.getToid() + " " + dep.getDependency_type() + "\n");
				}
			}
			writer.close();	
			return "Success";
		} catch (IOException e) {
			System.out.println(e.getMessage());
			return e.getMessage();
		}

	}

	public void setLogFilePath(String path) {
		logFile = path;
	}
}
