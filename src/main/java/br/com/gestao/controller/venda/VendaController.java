package br.com.gestao.controller.venda;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import br.com.gestao.controller.DefaultController;
import br.com.gestao.entity.Venda;
import br.com.gestao.service.VendaService;

@Controller
@RequestMapping("/venda/vendas")
public class VendaController extends DefaultController {

	private static final String PAGINA = "venda/vendas";
	private final VendaService vendaService;

	public VendaController(VendaService vendaService) {
		this.vendaService = vendaService;
	}

	@GetMapping
	public String listar(Model model) {
		model.addAttribute("vendas", vendaService.consultarTodas());
		return PAGINA;
	}

	@GetMapping("/detalhe")
	public String detalhe(Model model, @RequestParam Long id) {
		try {
			Venda venda = vendaService.consultarPorId(id);
			model.addAttribute("vendaDetalhe", venda);
			model.addAttribute("vendas", vendaService.consultarTodas());
		} catch (Exception e) {
			showError(model, e.getMessage());
			model.addAttribute("vendas", vendaService.consultarTodas());
		}
		return PAGINA;
	}

	@GetMapping("/cancelar")
	public String cancelar(Model model, RedirectAttributes redirectAttributes, @RequestParam Long id) {
		try {
			vendaService.cancelarVenda(id);
			showSucesso(redirectAttributes, "Venda #" + id + " cancelada!");
		} catch (Exception e) {
			showError(redirectAttributes, e.getMessage());
		}
		return "redirect:/venda/vendas";
	}

}
