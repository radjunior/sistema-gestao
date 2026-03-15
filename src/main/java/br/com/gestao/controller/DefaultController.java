package br.com.gestao.controller;

import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

public class DefaultController {

	private static final String ERRO = "erro";
	private static final String SUCESSO = "sucesso";
	
	public void showError(Model model, String message) {
		model.addAttribute(ERRO, message);
	}
	
	public void showError(RedirectAttributes redirectAttributes, String msg) {
		redirectAttributes.addFlashAttribute(ERRO, msg);
	}

	public void showSucesso(RedirectAttributes redirectAttributes, String msg) {
		redirectAttributes.addFlashAttribute(SUCESSO, msg);
	}
}
