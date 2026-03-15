package br.com.gestao.controller.cadastro;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import br.com.gestao.controller.DefaultController;
import br.com.gestao.entity.Subgrupo;
import br.com.gestao.service.GrupoService;

@Controller
@RequestMapping("/cadastro/sub-grupo")
public class SubgrupoController extends DefaultController {

	private static final String PAGINA = "cadastro/grupo";
	private static final String REDIRECT = "redirect:/cadastro/grupo";
	private GrupoService grupoService;

	public SubgrupoController(GrupoService grupoService) {
		this.grupoService = grupoService;
	}
	
	@GetMapping
	public String consultar(Model model, @RequestParam(required = false) Long id) {
		try {
			if (id != null) {
				model.addAttribute("sub", grupoService.consultarSubgrupoPorId(id));
			}
			carregarPagina(model);
		} catch (Exception e) {
			e.printStackTrace();
			showError(model, e.getMessage());
			carregarPagina(model);
		}
		return PAGINA;
	}

	@PostMapping
	public String salvar(Model model, RedirectAttributes ra, Subgrupo sub) {
		try {
			grupoService.salvarSubgrupo(sub);
			String msg = (sub.getId() == null) ? "Subgrupo cadastrado com sucesso!" : "Subgrupo atualizado com sucesso!";
			showSucesso(ra, msg);
			return REDIRECT;
		} catch (Exception e) {
			showError(model, e.getMessage());
			carregarPagina(model);
			model.addAttribute("sub", sub);
			return PAGINA;
		}
	}

	@PostMapping("/excluir")
	public String excluir(Model model, RedirectAttributes ra, Subgrupo sub) {
		try {
			grupoService.excluirSubgrupo(sub);
			showSucesso(ra, "Subgrupo excluído com sucesso!");
			return REDIRECT;
		} catch (Exception e) {
			e.printStackTrace();
			if (e.getMessage() != null && e.getMessage().contains("violates foreign key constraint")) {
				showError(model, "Não é possível excluir essa Subgrupo pois ela está relacionada com outra entidade!");
			} else {
				showError(model, e.getMessage());
			}
			carregarPagina(model);
			return PAGINA;
		}
	}

	private void carregarPagina(Model model) {
		model.addAttribute("grupos", grupoService.consultar());
		model.addAttribute("subgrupos", grupoService.consultarSubgrupos());
	}
}
