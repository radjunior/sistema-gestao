package br.com.gestao.controller.cadastro;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import br.com.gestao.controller.DefaultController;
import br.com.gestao.service.EstoqueService;
import br.com.gestao.service.EstoqueService.AjusteEstoque;
import br.com.gestao.service.EstoqueService.AjusteResultado;

@Controller
@RequestMapping("/cadastro/estoque")
public class EstoqueController extends DefaultController {

	private static final String PAGINA = "cadastro/estoque";
	private static final String REDIRECT = "redirect:/cadastro/estoque";

	private final EstoqueService estoqueService;

	public EstoqueController(EstoqueService estoqueService) {
		this.estoqueService = estoqueService;
	}

	@GetMapping
	public String listar(Model model) {
		carregar(model);
		return PAGINA;
	}

	@PostMapping("/ajuste")
	public String ajustar(RedirectAttributes ra,
			@RequestParam Long produtoId,
			@RequestParam Integer delta,
			@RequestParam(required = false) String motivo) {
		try {
			AjusteResultado r = estoqueService.ajustar(produtoId, delta, motivo);
			String sinal = r.delta() > 0 ? "+" : "";
			showSucesso(ra, "Estoque de '" + r.descricao() + "' ajustado ("
					+ sinal + r.delta() + "): " + r.quantidadeAnterior() + " -> " + r.quantidadeAtual() + ".");
		} catch (Exception e) {
			showError(ra, e.getMessage());
		}
		return REDIRECT;
	}

	@PostMapping("/ajuste-massa")
	public String ajustarEmMassa(RedirectAttributes ra,
			@RequestParam(required = false) List<Long> produtoIds,
			@RequestParam(required = false) List<Integer> deltas,
			@RequestParam(required = false) String motivo) {
		try {
			if (produtoIds == null || deltas == null || produtoIds.size() != deltas.size()) {
				throw new Exception("Listas de produto e ajuste inconsistentes!");
			}
			List<AjusteEstoque> ajustes = new ArrayList<>(produtoIds.size());
			for (int i = 0; i < produtoIds.size(); i++) {
				ajustes.add(new AjusteEstoque(produtoIds.get(i), deltas.get(i)));
			}
			List<AjusteResultado> resultados = estoqueService.ajustarEmMassa(ajustes, motivo);
			showSucesso(ra, resultados.size() + " produto(s) ajustado(s) com sucesso!");
		} catch (Exception e) {
			showError(ra, e.getMessage());
		}
		return REDIRECT;
	}

	private void carregar(Model model) {
		model.addAttribute("produtos", estoqueService.listarProdutos());
	}

}
