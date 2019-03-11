package eu.openreq.milla.controllers.test;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import eu.openreq.milla.controllers.MillaController;
import eu.openreq.milla.models.jira.Issue;
import eu.openreq.milla.models.json.*;
import eu.openreq.milla.services.FormatTransformerService;

import static org.junit.Assert.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class MillaControllerTest {
	
	private MockMvc mockMvc;

	  @Autowired
	  MillaController target;

	  @Before
	  public void setup() {
	    mockMvc = MockMvcBuilders.standaloneSetup(target).build();
	  }
	  
	  @Test
	  public void basicTest() throws Exception { 
	  mockMvc.perform(post("sendProjectToMulperi"))
	  	.andExpect(status().isOk());
	  }


  
}
