package br.com.gestao.controller.cadastro;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import br.com.gestao.controller.DefaultController;
import br.com.gestao.entity.Grupo;
import br.com.gestao.service.GrupoService;

@Controller
@RequestMapping("/cadastro/grupo")
public class GrupoController extends DefaultController {

	private static final String PAGINA = "cadastro/grupo";
	private static final String REDIRECT = "redirect:/cadastro/grupo";
	private GrupoService grupoService;

	public GrupoController(GrupoService grupoService) {
		this.grupoService = grupoService;
	}

	@GetMapping
	public String consultar(Model model, @RequestParam(required = false) Long id) {
		try {
			if (id != null) {
				model.addAttribute("grupo", grupoService.consultarPorId(id));
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
	public String salvar(Model model, RedirectAttributes ra, Grupo grupo) {
		try {
			grupoService.salvar(grupo);
			String msg = (grupo.getId() == null) ? "Grupo cadastrado com sucesso!" : "Grupo atualizado com sucesso!";
			showSucesso(ra, msg);
			return REDIRECT;
		} catch (Exception e) {
			showError(model, e.getMessage());
			carregarPagina(model);
			model.addAttribute("grupo", grupo);
			return PAGINA;
		}
	}

	@PostMapping("/excluir")
	public String excluir(Model model, RedirectAttributes ra, Grupo grupo) {
		try {
			grupoService.excluir(grupo);
			showSucesso(ra, "Grupo excluído com sucesso!");
			return REDIRECT;
		} catch (Exception e) {
			e.printStackTrace();
			if (e.getMessage().contains("violates foreign key constraint")) {
				showError(model, "Não é possível excluir essa Grupo pois ela está relacionada com outra entidade!");
			} else {
				showError(model, e.getMessage());
			}
			carregarPagina(model);
			return PAGINA;
		}
	}

	private void carregarPagina(Model model) {
		try {
			model.addAttribute("grupos", grupoService.consultar());
			model.addAttribute("subgrupos", grupoService.consultarSubgrupos());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
