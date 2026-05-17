package br.com.gestao.controller.venda;

import java.math.BigDecimal;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import br.com.gestao.controller.DefaultController;
import br.com.gestao.entity.Caixa;
import br.com.gestao.service.CaixaService;

@Controller
@RequestMapping("/venda/caixa")
public class CaixaController extends DefaultController {

	private static final String PAGINA = "venda/caixa";
	private static final String REDIRECT = "redirect:/venda/caixa";

	private final CaixaService caixaService;

	public CaixaController(CaixaService caixaService) {
		this.caixaService = caixaService;
	}

	@GetMapping
	public String index(Model model) {
		Caixa aberto = caixaService.getCaixaAberto();
		model.addAttribute("caixaAberto", aberto);
		if (aberto != null) {
			model.addAttribute("resumo", caixaService.calcularResumo(aberto));
		}
		model.addAttribute("historico", caixaService.consultarHistorico());
		return PAGINA;
	}

	@GetMapping("/{id}")
	public String detalhe(@PathVariable Long id, Model model) {
		try {
			Caixa caixa = caixaService.consultarPorId(id);
			model.addAttribute("caixaDetalhe", caixa);
			model.addAttribute("resumoDetalhe", caixaService.calcularResumo(caixa));
		} catch (Exception e) {
			showError(model, e.getMessage());
		}
		model.addAttribute("caixaAberto", caixaService.getCaixaAberto());
		model.addAttribute("historico", caixaService.consultarHistorico());
		return PAGINA;
	}

	@PostMapping("/abrir")
	public String abrir(RedirectAttributes ra,
			@RequestParam(required = false) BigDecimal valorAbertura,
			@RequestParam(required = false) String observacao) {
		try {
			caixaService.abrirCaixa(valorAbertura, observacao);
			showSucesso(ra, "Caixa aberto com sucesso!");
		} catch (Exception e) {
			showError(ra, e.getMessage());
		}
		return REDIRECT;
	}

	@PostMapping("/sangria")
	public String sangria(RedirectAttributes ra,
			@RequestParam BigDecimal valor,
			@RequestParam(required = false) String descricao) {
		try {
			caixaService.registrarSangria(valor, descricao);
			showSucesso(ra, "Sangria registrada!");
		} catch (Exception e) {
			showError(ra, e.getMessage());
		}
		return REDIRECT;
	}

	@PostMapping("/suprimento")
	public String suprimento(RedirectAttributes ra,
			@RequestParam BigDecimal valor,
			@RequestParam(required = false) String descricao) {
		try {
			caixaService.registrarSuprimento(valor, descricao);
			showSucesso(ra, "Suprimento registrado!");
		} catch (Exception e) {
			showError(ra, e.getMessage());
		}
		return REDIRECT;
	}

	@PostMapping("/fechar")
	public String fechar(RedirectAttributes ra,
			@RequestParam BigDecimal valorInformado,
			@RequestParam(required = false) String observacao) {
		try {
			Caixa caixa = caixaService.fecharCaixa(valorInformado, observacao);
			showSucesso(ra, "Caixa fechado. Diferenca: R$ " + caixa.getDiferenca());
		} catch (Exception e) {
			showError(ra, e.getMessage());
		}
		return REDIRECT;
	}

}
