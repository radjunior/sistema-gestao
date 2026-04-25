package br.com.gestao.controller;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import br.com.gestao.entity.Empresa;
import br.com.gestao.entity.Usuario;
import br.com.gestao.entity.VersaoSistema;
import br.com.gestao.service.ContextoUsuarioService;
import br.com.gestao.service.VersaoSistemaService;

@ControllerAdvice
public class GlobalModelAttributes {

	private final ContextoUsuarioService contextoUsuarioService;
	private final VersaoSistemaService versaoSistemaService;

	public GlobalModelAttributes(ContextoUsuarioService contextoUsuarioService, VersaoSistemaService versaoSistemaService) {
		this.contextoUsuarioService = contextoUsuarioService;
		this.versaoSistemaService = versaoSistemaService;
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

	@ModelAttribute("versoesPendentes")
	public java.util.List<VersaoSistema> versoesPendentes() {
		return versaoSistemaService.consultarPendentesDoUsuarioLogado();
	}

	@ModelAttribute("versoesPendentesJson")
	public String versoesPendentesJson() {
		StringBuilder json = new StringBuilder("[");
		java.util.List<VersaoSistema> versoes = versaoSistemaService.consultarPendentesDoUsuarioLogado();
		for (int i = 0; i < versoes.size(); i++) {
			VersaoSistema versao = versoes.get(i);
			if (i > 0) {
				json.append(",");
			}
			json.append("{")
					.append("\"id\":").append(versao.getId()).append(",")
					.append("\"titulo\":\"").append(escapeJson(versao.getTitulo())).append("\",")
					.append("\"descricao\":\"").append(escapeJson(versao.getDescricao())).append("\"")
					.append("}");
		}
		json.append("]");
		return json.toString();
	}

	private String escapeJson(String valor) {
		if (valor == null) {
			return "";
		}
		return valor
				.replace("\\", "\\\\")
				.replace("\"", "\\\"")
				.replace("\r", "")
				.replace("\n", "\\n")
				.replace("\t", "\\t")
				.replace("</", "<\\/");
	}
}
