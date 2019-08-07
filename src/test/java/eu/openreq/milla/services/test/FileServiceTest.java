package eu.openreq.milla.services.test;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.AutoConfigureWebClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertEquals;

import eu.openreq.milla.MillaApplication;
import eu.openreq.milla.models.json.Dependency;
import eu.openreq.milla.models.json.Dependency_status;
import eu.openreq.milla.services.FileService;

import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes=MillaApplication.class)
@SpringBootTest
@AutoConfigureWebClient
public class FileServiceTest {	
	
	@MockBean
	FileWriter writer;
	
	@Autowired
	FileService fs;
    
	List<Dependency> dependencies;
	
    @Before
    public void setUp() throws Exception {    
    	Dependency dep = new Dependency();
		dep.setId("test");
		dep.setStatus(Dependency_status.ACCEPTED);
		
		Dependency dep2 = new Dependency();
		dep2.setId("test2");
		dep2.setStatus(Dependency_status.REJECTED);
		dependencies = Arrays.asList(dep, dep2);
    }

    @Test
    public void testDependencyLogSuccess() throws Exception {
    	Path newFilePath = Paths.get("logs/testing.log");
        Files.createFile(newFilePath);
        
    	fs.setLogFilePath("logs/testing.log");
    	String result = fs.logDependencies(dependencies);
    	
    	Files.delete(newFilePath);
    	
    	assertEquals("Success", result);
    }
    
    
}
