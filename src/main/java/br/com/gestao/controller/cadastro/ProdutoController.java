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
import br.com.gestao.entity.ProdutoVariacao;
import br.com.gestao.service.CategoriaService;
import br.com.gestao.service.GrupoService;
import br.com.gestao.service.ProdutoService;

@Controller
@RequestMapping("/cadastro")
public class ProdutoController extends DefaultController {

	private final static String PAGINA = "cadastro/produto";
	private final static String REDIRECT = "redirect:/cadastro/produto";
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
	public String consultar(Model m, @RequestParam(required = false) Long id,
			@RequestParam(required = false) Long var) {
		try {
			if (id != null) {
				m.addAttribute("produto", produtoService.consultarProdutoPorId(id));
			}
			if (var != null) {
				m.addAttribute("produtoVariacao", produtoService.consultarVariacaoPorId(var));
			}
			carregarPagina(m);
		} catch (Exception e) {
			e.printStackTrace();
			showError(m, e.getMessage());
			carregarPagina(m);
		}
		return PAGINA;
	}

	@PostMapping("/produto")
	public String salvar(Model m, RedirectAttributes ra, Produto produto) {
		try {
			produtoService.salvarProduto(produto);
			String msg = (produto.getId() == null) ? "Produto cadastrada com sucesso!"
					: "Produto atualizada com sucesso!";
			showSucesso(ra, msg);
			return REDIRECT;
		} catch (Exception e) {
			showError(m, e.getMessage());
			carregarPagina(m);
			m.addAttribute("produto", produto);
			return PAGINA;
		}
	}

	@PostMapping("/produto/excluir")
	public String excluir(Model m, RedirectAttributes ra, Produto produto) {
		try {
			produtoService.excluirProduto(produto);
			showSucesso(ra, "Produto excluída com sucesso!");
			return REDIRECT;
		} catch (Exception e) {
			showError(m, e.getMessage());
			carregarPagina(m);
			return PAGINA;
		}
	}

	// ----------------------
	// Variação do Produto
	// ----------------------

	@PostMapping("/produto-variacao")
	public String salvarVariacao(Model m, RedirectAttributes ra, ProdutoVariacao variacao) {
		try {
			produtoService.salvarVariacao(variacao);
			String msg = (variacao.getId() == null) ? "Variacao cadastrada com sucesso!"
					: "Variacao atualizada com sucesso!";
			showSucesso(ra, msg);
			return REDIRECT;
		} catch (Exception e) {
			showError(m, e.getMessage());
			carregarPagina(m);
			m.addAttribute("variacao", variacao);
			return PAGINA;
		}
	}

	@PostMapping("/produto-variacao/excluir")
	public String excluirVariacao(Model m, RedirectAttributes ra, ProdutoVariacao variacao) {
		try {
			produtoService.excluirVariacao(variacao);
			showSucesso(ra, "Variacao excluída com sucesso!");
			return REDIRECT;
		} catch (Exception e) {
			showError(m, e.getMessage());
			carregarPagina(m);
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
