package br.com.gestao.controller.cadastro;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import br.com.gestao.controller.DefaultController;
import br.com.gestao.entity.Fornecedor;
import br.com.gestao.service.FornecedorService;

@Controller
@RequestMapping("/cadastro/fornecedor")
public class FornecedorController extends DefaultController {

	private static final String PAGINA = "cadastro/fornecedor";
	private static final String REDIRECT = "redirect:/cadastro/fornecedor";
	private final FornecedorService fornecedorService;

	public FornecedorController(FornecedorService fornecedorService) {
		this.fornecedorService = fornecedorService;
	}

	@GetMapping
	public String consultar(Model model, @RequestParam(required = false) Long id) {
		try {
			if (id != null) {
				model.addAttribute("fornecedor", fornecedorService.consultarPorId(id));
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
	public String salvar(Model model, RedirectAttributes ra, Fornecedor fornecedor) {
		try {
			fornecedorService.salvar(fornecedor);
			showSucesso(ra, (fornecedor.getId() == null) ? "Fornecedor cadastrado com sucesso!"
					: "Fornecedor atualizado com sucesso!");
			return REDIRECT;
		} catch (Exception e) {
			showError(model, e.getMessage());
			carregarPagina(model);
			model.addAttribute("fornecedor", fornecedor);
			return PAGINA;
		}
	}

	@PostMapping("/excluir")
	public String excluir(Model model, RedirectAttributes ra, Fornecedor fornecedor) {
		try {
			fornecedorService.excluir(fornecedor);
			showSucesso(ra, "Fornecedor excluido com sucesso!");
			return REDIRECT;
		} catch (Exception e) {
			e.printStackTrace();
			if (e.getMessage() != null && e.getMessage().contains("violates foreign key constraint")) {
				showError(model,
						"Nao e possivel excluir esse Fornecedor pois ele esta relacionado com outra entidade!");
			} else {
				showError(model, e.getMessage());
			}
			carregarPagina(model);
			return PAGINA;
		}
	}

	private void carregarPagina(Model model) {
		model.addAttribute("fornecedores", fornecedorService.consultar());
	}

}
