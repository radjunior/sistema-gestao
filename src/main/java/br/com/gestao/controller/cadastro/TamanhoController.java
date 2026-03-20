package br.com.gestao.controller.cadastro;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import br.com.gestao.controller.DefaultController;
import br.com.gestao.entity.Tamanho;
import br.com.gestao.service.TamanhoService;

@Controller
@RequestMapping("/cadastro/tamanho")
public class TamanhoController extends DefaultController {

	private static final String PAGINA = "cadastro/tamanho";
	private static final String REDIRECT = "redirect:/cadastro/tamanho";

	private final TamanhoService tamanhoService;

	public TamanhoController(TamanhoService tamanhoService) {
		this.tamanhoService = tamanhoService;
	}

	@GetMapping
	public String consultar(Model model, @RequestParam(required = false) Long id) {
		try {
			if (id != null) {
				model.addAttribute("tamanho", tamanhoService.consultarPorId(id));
			}
			carregarPagina(model);
		} catch (Exception e) {
			showError(model, e.getMessage());
			carregarPagina(model);
		}
		return PAGINA;
	}

	@PostMapping
	public String salvar(Model model, RedirectAttributes redirectAttributes, Tamanho tamanho) {
		try {
			tamanhoService.salvar(tamanho);
			showSucesso(redirectAttributes,
					tamanho.getId() == null ? "Tamanho cadastrado com sucesso!" : "Tamanho atualizado com sucesso!");
			return REDIRECT;
		} catch (Exception e) {
			showError(model, e.getMessage());
			model.addAttribute("tamanho", tamanho);
			carregarPagina(model);
			return PAGINA;
		}
	}

	@PostMapping("/excluir")
	public String excluir(Model model, RedirectAttributes redirectAttributes, Tamanho tamanho) {
		try {
			tamanhoService.excluir(tamanho);
			showSucesso(redirectAttributes, "Tamanho excluido com sucesso!");
			return REDIRECT;
		} catch (Exception e) {
			showError(model, e.getMessage());
			carregarPagina(model);
			return PAGINA;
		}
	}

	private void carregarPagina(Model model) {
		model.addAttribute("tamanhos", tamanhoService.consultar());
	}
}
