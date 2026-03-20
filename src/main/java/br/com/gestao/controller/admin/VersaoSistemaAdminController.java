package br.com.gestao.controller.admin;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import br.com.gestao.controller.DefaultController;
import br.com.gestao.entity.VersaoSistema;
import br.com.gestao.service.VersaoSistemaService;

@Controller
@RequestMapping
public class VersaoSistemaAdminController extends DefaultController {

	private static final String PAGINA = "admin/versoes";
	private static final String REDIRECT = "redirect:/admin/versoes";

	private final VersaoSistemaService versaoSistemaService;

	public VersaoSistemaAdminController(VersaoSistemaService versaoSistemaService) {
		this.versaoSistemaService = versaoSistemaService;
	}

	@GetMapping("/admin/versoes")
	public String consultar(Model model, @RequestParam(required = false) Long id) {
		if (id != null) {
			model.addAttribute("versao", versaoSistemaService.consultarPorId(id));
		}
		carregarPagina(model);
		return PAGINA;
	}

	@PostMapping("/admin/versoes")
	public String salvar(Model model, RedirectAttributes redirectAttributes, VersaoSistema versao) {
		try {
			boolean novo = versao.getId() == null;
			versaoSistemaService.salvar(versao);
			showSucesso(redirectAttributes, novo ? "Versao cadastrada com sucesso!" : "Versao atualizada com sucesso!");
			return REDIRECT;
		} catch (Exception e) {
			showError(model, e.getMessage());
			model.addAttribute("versao", versao);
			carregarPagina(model);
			return PAGINA;
		}
	}

	@PostMapping("/admin/versoes/excluir")
	public String excluir(Model model, RedirectAttributes redirectAttributes, @RequestParam Long id) {
		try {
			versaoSistemaService.excluir(id);
			showSucesso(redirectAttributes, "Versao excluida com sucesso!");
			return REDIRECT;
		} catch (Exception e) {
			showError(model, e.getMessage());
			carregarPagina(model);
			return PAGINA;
		}
	}

	@PostMapping("/versoes/{id}/visualizar")
	public ResponseEntity<Void> visualizar(@PathVariable Long id) {
		versaoSistemaService.registrarVisualizacao(id);
		return ResponseEntity.ok().build();
	}

	private void carregarPagina(Model model) {
		model.addAttribute("versoes", versaoSistemaService.consultarTodas());
	}
}
