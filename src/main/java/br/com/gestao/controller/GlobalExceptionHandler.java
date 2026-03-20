package br.com.gestao.controller;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import br.com.gestao.service.LogErroAplicacaoService;
import jakarta.servlet.http.HttpServletRequest;

@ControllerAdvice
public class GlobalExceptionHandler {

	private final LogErroAplicacaoService logErroAplicacaoService;

	public GlobalExceptionHandler(LogErroAplicacaoService logErroAplicacaoService) {
		this.logErroAplicacaoService = logErroAplicacaoService;
	}

	@ExceptionHandler(Exception.class)
	public String handleException(Exception ex, HttpServletRequest request) {
		logErroAplicacaoService.registrarErro(ex, request, "WEB");
		return "error/500";
	}

}
