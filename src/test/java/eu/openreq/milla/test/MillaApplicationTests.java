package eu.openreq.milla.test;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import eu.openreq.milla.MillaApplication;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes=MillaApplication.class)
public class MillaApplicationTests {

	@Test
	public void contextLoads() {
	}

}
