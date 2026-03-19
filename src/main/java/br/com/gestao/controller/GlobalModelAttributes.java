package br.com.gestao.controller;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import br.com.gestao.entity.Empresa;
import br.com.gestao.entity.Usuario;
import br.com.gestao.service.ContextoUsuarioService;

@ControllerAdvice
public class GlobalModelAttributes {

	private final ContextoUsuarioService contextoUsuarioService;

	public GlobalModelAttributes(ContextoUsuarioService contextoUsuarioService) {
		this.contextoUsuarioService = contextoUsuarioService;
	}

	@ModelAttribute("usuarioLogado")
	public Usuario usuarioLogado() {
		return contextoUsuarioService.getUsuarioLogado();
	}

	@ModelAttribute("empresaAtual")
	public Empresa empresaAtual() {
		return contextoUsuarioService.getEmpresaLogada();
	}

	@ModelAttribute("adminSaas")
	public boolean adminSaas() {
		return contextoUsuarioService.isAdminSaas();
	}
}
