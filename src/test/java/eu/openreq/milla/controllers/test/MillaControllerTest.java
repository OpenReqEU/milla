package eu.openreq.milla.controllers.test;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;


import eu.openreq.milla.MillaApplication;
import eu.openreq.milla.controllers.MillaController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes=MillaApplication.class)
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
		  	.andExpect(status().isNotFound());
	  }


  
}
