//package eu.openreq.milla.controllers;
//
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.boot.autoconfigure.SpringBootApplication;
//import org.springframework.stereotype.Controller;
//import org.springframework.ui.Model;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RequestMethod;
//import org.springframework.web.bind.annotation.ResponseBody;
//
//import springfox.documentation.annotations.ApiIgnore;
//
//@SpringBootApplication
//@Controller
//public class TestingController {
//	
//	@Value("${milla.mulperiAddress}")
//    private String mulperiAddress;
//	
//	@RequestMapping(value = "/example/gui", method = RequestMethod.GET)
//	@ApiIgnore
//	@ResponseBody
//	public String exampleGUI(Model model) {
//
//		model.addAttribute("mulperiAddress", mulperiAddress);
//		return "exampleGUI";
//	}
//}
