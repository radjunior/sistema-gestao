package br.com.gestao.controller.financeiro;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import br.com.gestao.controller.DefaultController;
import br.com.gestao.entity.ConfiguracaoFinanceira;
import br.com.gestao.service.ConfiguracaoFinanceiraService;

@Controller
@RequestMapping("/financeiro/configuracao")
public class ConfiguracaoFinanceiraController extends DefaultController {

	private static final String PAGINA = "financeiro/configuracao";
	private static final String REDIRECT = "redirect:/financeiro/configuracao";

	private final ConfiguracaoFinanceiraService service;

	public ConfiguracaoFinanceiraController(ConfiguracaoFinanceiraService service) {
		this.service = service;
	}

	@GetMapping
	public String abrir(Model model) {
		model.addAttribute("config", service.obterOuCriarPadrao());
		return PAGINA;
	}

	@PostMapping
	public String salvar(Model model, RedirectAttributes redirectAttributes, ConfiguracaoFinanceira config) {
		try {
			service.salvar(config);
			showSucesso(redirectAttributes, "Configuracao financeira atualizada com sucesso!");
			return REDIRECT;
		} catch (Exception e) {
			showError(model, e.getMessage());
			model.addAttribute("config", config);
			return PAGINA;
		}
	}

}
