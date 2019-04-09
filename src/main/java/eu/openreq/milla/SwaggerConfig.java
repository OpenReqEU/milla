package eu.openreq.milla;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@EnableSwagger2
public class SwaggerConfig {
    @Bean
    public Docket productApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .select()
                .apis(RequestHandlerSelectors.basePackage("eu.openreq.milla.controllers"))    
                .paths(PathSelectors.any())  
                .build()
                .apiInfo(metaData());
             
    }
    
    private ApiInfo metaData() {
      return new ApiInfoBuilder()
              .title("Milla")
              .version("1.13")
              .description("Milla is mostly Qt Jira-specific orchestrator service for the OpenReq Qt trial that operates between Qt Jira and the OpenReq infrastructure. Milla has five main functionalities divided into three different controllers. <br>\r\n" + 
                	"<b>Milla controller:</b>" +
                	"<br>1) Milla fetches all or updated data from Qt Jira and stores in a caching manner the data in the Mallikas database service. " + 
              		"<br>2) Milla orchestrates the communication to Mallikas database service so that a project, requirement etc. can be manipulated (CRUD manner) in Mallikas database service in OpenReq JSON format." + 
              		"<br>3) Milla orchestrates the data from Mallikas database service to Mulperi service in order to manage dependencies, carry out analyses etc. \n" + 
              		"<br><b>Detection controller:</b><br>" +
              		"4) Milla calls the natural language processing services of OpenReq infrastructre that can detect from the requirements text hidden or implicit dependencies to other requirements.  <br>\r\n" + 
              		"<b>Qt controller:</b><br>" +
            		"5) Milla provides the access point for the Qt Jira dependency browser in the Qt trial. <br>\r\n" )
              .build();
  }
}