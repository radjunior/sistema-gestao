package br.com.gestao.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class HomeController {

	@GetMapping("/")
	public String index() {
		return "redirect:/home";
	}

	@GetMapping("/home")
	public String getHome() {
		return "home";
	}

	@GetMapping({"/favicon.ico", "/apple-touch-icon.png", "/apple-touch-icon-precomposed.png",
			"/apple-touch-icon-120x120.png", "/apple-touch-icon-120x120-precomposed.png"})
	@ResponseBody
	public ResponseEntity<Void> favicons() {
		return ResponseEntity.noContent().build();
	}
}
