package br.com.gestao.controller.cadastro;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import br.com.gestao.controller.DefaultController;
import br.com.gestao.entity.Marca;

@Controller
@RequestMapping("/cadastro/marca")
public class MarcaController extends DefaultController {

	private static final String PAGINA = "cadastro/marca";
	private static final String REDIRECT = "redirect:/cadastro/marca";
	private final MarcaService marcaService;

	public MarcaController(MarcaService marcaService) {
		this.marcaService = marcaService;
	}

	@GetMapping
	public String consultar(Model model, @RequestParam(required = false) Long id) {
		try {
			if (id != null) {
				model.addAttribute("marca", marcaService.consultarPorId(id));
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
	public String salvar(Model model, RedirectAttributes redirectAttributes, Marca marca) {
		try {
			marcaService.salvar(marca);
			showSucesso(redirectAttributes,
					(marca.getId() == null) ? "Marca cadastrada com sucesso!" : "Marca atualizada com sucesso!");
			return REDIRECT;
		} catch (Exception e) {
			showError(model, e.getMessage());
			carregarPagina(model);
			model.addAttribute("marca", marca);
			return PAGINA;
		}
	}

	@PostMapping("/excluir")
	public String excluir(Model model, RedirectAttributes redirectAttributes, Marca marca) {
		try {
			marcaService.excluir(marca);
			showSucesso(redirectAttributes, "Marca excluída com sucesso!");
			return REDIRECT;
		} catch (Exception e) {
			showError(model, e.getMessage());
			carregarPagina(model);
			return PAGINA;
		}
	}
	
	private void carregarPagina(Model model) {
	    model.addAttribute("marcas", marcaService.consultar());
	}

}