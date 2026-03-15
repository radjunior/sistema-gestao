package br.com.gestao.controller.cadastro;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import br.com.gestao.controller.DefaultController;
import br.com.gestao.entity.Produto;
import br.com.gestao.service.CategoriaService;
import br.com.gestao.service.GrupoService;
import br.com.gestao.service.MarcaService;
import br.com.gestao.service.ProdutoService;

@Controller
@RequestMapping("/cadastro")
public class ProdutoController extends DefaultController {

	private static final String PAGINA = "cadastro/produto";
	private static final String REDIRECT = "redirect:/cadastro/produto";
	private final ProdutoService produtoService;
	private final MarcaService marcaService;
	private final CategoriaService categoriaService;
	private final GrupoService grupoService;

	public ProdutoController(ProdutoService produtoService, MarcaService marcaService,
			CategoriaService categoriaService, GrupoService grupoService) {
		this.produtoService = produtoService;
		this.marcaService = marcaService;
		this.categoriaService = categoriaService;
		this.grupoService = grupoService;
	}

	@GetMapping("/produto")
	public String consultar(Model model, @RequestParam(required = false) Long id) {
		try {
			if (id != null) {
				model.addAttribute("produto", produtoService.consultarProdutoPorId(id));
			}
			carregarPagina(model);
		} catch (Exception e) {
			e.printStackTrace();
			showError(model, e.getMessage());
			carregarPagina(model);
		}
		return PAGINA;
	}

	@PostMapping("/produto")
	public String salvar(Model model, RedirectAttributes redirectAttributes, Produto produto) {
		try {
			produtoService.salvarProduto(produto);
			String msg = (produto.getId() == null) ? "Produto cadastrado com sucesso!"
					: "Produto atualizado com sucesso!";
			showSucesso(redirectAttributes, msg);
			return REDIRECT;
		} catch (Exception e) {
			showError(model, e.getMessage());
			carregarPagina(model);
			model.addAttribute("produto", produto);
			return PAGINA;
		}
	}

	@PostMapping("/produto/excluir")
	public String excluir(Model model, RedirectAttributes redirectAttributes, Produto produto) {
		try {
			produtoService.excluirProduto(produto);
			showSucesso(redirectAttributes, "Produto excluído com sucesso!");
			return REDIRECT;
		} catch (Exception e) {
			e.printStackTrace();
			if (e.getMessage() != null && e.getMessage().contains("violates foreign key constraint")) {
				showError(model, "Não é possível excluir esse produto pois ele está relacionado com outra entidade!");
			} else {
				showError(model, e.getMessage());
			}
			carregarPagina(model);
			return PAGINA;
		}
	}

	private void carregarPagina(Model model) {
		model.addAttribute("produtos", produtoService.consultarProduto());
		model.addAttribute("marcas", marcaService.consultar());
		model.addAttribute("categorias", categoriaService.consultar());
		model.addAttribute("grupos", grupoService.consultar());
		model.addAttribute("subgrupos", grupoService.consultarSubgrupos());
	}
}
