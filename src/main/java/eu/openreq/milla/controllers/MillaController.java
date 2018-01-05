package eu.openreq.milla.controllers;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import eu.openreq.milla.models.jira.Jira;
import eu.openreq.milla.models.mulson.Requirement;
import eu.openreq.milla.services.FormatTransformerService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@SpringBootApplication
@Controller
@RequestMapping(value = "/")
public class MillaController {

	@Value("${milla.mulperiAddress}")
    private String mulperiAddress;
	
	@ApiOperation(value = "Relay GET to Mulperi",
		    notes = "Get a configuration from Mulperi")
	@ResponseBody
	@RequestMapping(value = "relay/{path}", method = RequestMethod.GET)
	public ResponseEntity<?> getFromMulperi(@PathVariable("path") String path) {
		
		RestTemplate rt = new RestTemplate();
		
		String actualPath = getActualPath(path);
		
		String completeAddress = mulperiAddress + actualPath;
		
		return rt.getForEntity(completeAddress, String.class);
	}
	
	@ApiOperation(value = "Relay POST to Mulperi",
		    notes = "Post a model or configuration request to Mulperi")
	@ResponseBody
	@RequestMapping(value = "relay/{path}", method = RequestMethod.POST)
	public ResponseEntity<?> postToMulperi(@RequestBody String data, @PathVariable("path") String path) {
		
		RestTemplate rt = new RestTemplate();
		
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		
		String actualPath = getActualPath(path);
		
		String completeAddress = mulperiAddress + actualPath;
		
		HttpEntity<String> entity = new HttpEntity<String>(data, headers);
		
		ResponseEntity<?> response = null;
		System.out.println("Lähetys Mulperille: " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date()));
		try {
			response = rt.postForEntity(completeAddress, entity, String.class);
		} catch (HttpClientErrorException e) {
			return new ResponseEntity<>("Mulperi error:\n\n" + e.getResponseBodyAsString(), e.getStatusCode());
		}
		
		return response;
	}

	private String getActualPath(String path) {
		if (path.equals("mulson")) return "models/mulson";
		if (path.equals("reqif")) return "models/reqif";
		if (path.contains("configure:")) {
			String modelName = path.split(":", 2)[1];			
			return "models/" + modelName + "/configurations";
		}
			
		return path;
	}
	
	/**
	 * Inputs an array of search queries (return array of issues), ex:
	 [
	    "https://bugreports.qt.io/rest/api/2/search?jql=\"Epic Link\"=QTBUG-60623",
	    "https://bugreports.qt.io/rest/api/2/search?jql=issue = QTBUG-60467"
	 ]
	 * @param paths
	 * @return
	 * @throws JsonProcessingException
	 * @throws ExecutionException 
	 * @throws InterruptedException 
	 */
	@ApiOperation(value = "Parse Jira",
		    notes = "Generate a model from an array of Jira search queries (that return an array of issues)",
		    response = String.class)
	@ApiResponses(value = { 
			@ApiResponse(code = 201, message = "Success, returns the name of the generated model"),
			@ApiResponse(code = 400, message = "Failure, ex. malformed JSON"),
			@ApiResponse(code = 500, message = "Failure, ex. invalid URLs")}) 
	@ResponseBody
	@RequestMapping(value = "jira", method = RequestMethod.POST)
	public ResponseEntity<?> loadFromJira(@RequestBody List<String> paths) throws JsonProcessingException, InterruptedException, ExecutionException {
		FormatTransformerService transformer = new FormatTransformerService();
		
		RestTemplate rt = new RestTemplate();
		
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		
		ConcurrentHashMap<String, Jira> jiras = new ConcurrentHashMap<>();
		
		ForkJoinPool customThreadPool = new ForkJoinPool(8);
	    customThreadPool.submit(
	      () -> paths.parallelStream().forEach((url) -> {
				ResponseEntity<Jira> jiraResponse = rt.getForEntity(url, Jira.class);
				jiras.put(url, jiraResponse.getBody());
			})
	    ).get();
		
		Collection<Requirement> requirements = transformer.convertJirasToMulson(jiras.values());
		
		ObjectMapper mapper = new ObjectMapper();
		String mulsonString = mapper.writeValueAsString(requirements);

		return this.postToMulperi(mulsonString, "mulson");
	}
	
	@RequestMapping(value = "/example/gui", method = RequestMethod.GET)
	public String exampleGUI(Model model) {

		model.addAttribute("mulperiAddress", mulperiAddress);
		return "exampleGUI";
	}
}
