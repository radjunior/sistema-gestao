package br.com.gestao.controller;

import org.springframework.boot.webmvc.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;

@Controller
public class ErroController implements ErrorController {

	@RequestMapping("/403")
	public String acessoNegado() {
		return "error/403";
	}

	@RequestMapping("/error")
	public String handleError(HttpServletRequest request) {
		Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
		if (status != null) {
			int statusCode = Integer.parseInt(status.toString());
			if (statusCode == 404) {
				return "error/404";
			}
			if (statusCode == 403) {
				return "error/403";
			}
			if (statusCode == 500) {
				return "error/500";
			}
		}
		return "error/generic";
	}
}
