package br.com.gestao.controller.cadastro;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import br.com.gestao.controller.DefaultController;
import br.com.gestao.entity.Categoria;
import br.com.gestao.service.CategoriaService;

@Controller
@RequestMapping("/cadastro/categoria")
public class CategoriaController extends DefaultController {

	private static final String PAGINA = "cadastro/categoria";
	private static final String REDIRECT = "redirect:/cadastro/categoria";
	private final CategoriaService categoriaService;

	public CategoriaController(CategoriaService categoriaService) {
		this.categoriaService = categoriaService;
	}

	@GetMapping
	public String consultar(Model model, @RequestParam(required = false) Long id) {
		try {
			if (id != null) {
				model.addAttribute("categoria", categoriaService.consultarPorId(id));
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
	public String salvar(Model model, RedirectAttributes redirectAttributes, Categoria categoria) {
		try {
			categoriaService.salvar(categoria);
			showSucesso(redirectAttributes,
					(categoria.getId() == null) ? "Categoria cadastrada com sucesso!" : "Categoria atualizada com sucesso!");
			return REDIRECT;
		} catch (Exception e) {
			showError(model, e.getMessage());
			carregarPagina(model);
			model.addAttribute("categoria", categoria);
			return PAGINA;
		}
	}

	@PostMapping("/excluir")
	public String excluir(Model model, RedirectAttributes redirectAttributes, Categoria categoria) {
		try {
			categoriaService.excluir(categoria);
			showSucesso(redirectAttributes, "Categoria excluída com sucesso!");
			return REDIRECT;
		} catch (Exception e) {
			e.printStackTrace();
			if (e.getMessage().contains("violates foreign key constraint")) {
				showError(model, "Não é possível excluir essa Categoria pois ela está relacionada com outra entidade!");
			} else {
				showError(model, e.getMessage());
			}
			carregarPagina(model);
			return PAGINA;
		}
	}
	
	private void carregarPagina(Model model) {
	    model.addAttribute("categorias", categoriaService.consultar());
	}

}
