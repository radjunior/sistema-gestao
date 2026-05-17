package br.com.gestao.controller.venda;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import br.com.gestao.controller.DefaultController;
import br.com.gestao.entity.Venda;
import br.com.gestao.service.DevolucaoService;
import br.com.gestao.service.DevolucaoService.DevolucaoRequest;
import br.com.gestao.service.VendaService;

@Controller
@RequestMapping("/venda/devolucao")
public class DevolucaoController extends DefaultController {

	private static final String PAGINA = "venda/devolucao";

	private final DevolucaoService devolucaoService;
	private final VendaService vendaService;

	public DevolucaoController(DevolucaoService devolucaoService, VendaService vendaService) {
		this.devolucaoService = devolucaoService;
		this.vendaService = vendaService;
	}

	@GetMapping("/{vendaId}")
	public String form(@PathVariable Long vendaId, Model model) {
		try {
			Venda venda = vendaService.consultarPorId(vendaId);
			model.addAttribute("venda", venda);
			model.addAttribute("itens", devolucaoService.listarItensDevolviveis(vendaId));
			model.addAttribute("devolucoes", devolucaoService.listarDevolucoesVenda(vendaId));
		} catch (Exception e) {
			showError(model, e.getMessage());
		}
		return PAGINA;
	}

	@PostMapping("/{vendaId}")
	public String registrar(@PathVariable Long vendaId, RedirectAttributes ra,
			@RequestParam(required = false) List<Long> vendaItemIds,
			@RequestParam(required = false) List<Integer> quantidades,
			@RequestParam(required = false) String motivo) {
		try {
			List<DevolucaoRequest> requests = new ArrayList<>();
			if (vendaItemIds != null && quantidades != null) {
				int n = Math.min(vendaItemIds.size(), quantidades.size());
				for (int i = 0; i < n; i++) {
					requests.add(new DevolucaoRequest(vendaItemIds.get(i), quantidades.get(i)));
				}
			}
			devolucaoService.registrarDevolucao(vendaId, requests, motivo);
			showSucesso(ra, "Devolucao registrada com sucesso!");
			return "redirect:/venda/devolucao/" + vendaId;
		} catch (Exception e) {
			showError(ra, e.getMessage());
			return "redirect:/venda/devolucao/" + vendaId;
		}
	}

}
