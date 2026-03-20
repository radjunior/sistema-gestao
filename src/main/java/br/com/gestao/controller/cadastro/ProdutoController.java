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
import br.com.gestao.service.GrupoService;
import br.com.gestao.service.MarcaService;
import br.com.gestao.service.ProdutoService;
import br.com.gestao.service.TamanhoService;

@Controller
@RequestMapping("/cadastro")
public class ProdutoController extends DefaultController {

	private static final String PAGINA = "cadastro/produto";
	private static final String REDIRECT = "redirect:/cadastro/produto";
	private final ProdutoService produtoService;
	private final MarcaService marcaService;
	private final GrupoService grupoService;
	private final TamanhoService tamanhoService;

	public ProdutoController(ProdutoService produtoService, MarcaService marcaService, GrupoService grupoService,
			TamanhoService tamanhoService) {
		this.produtoService = produtoService;
		this.marcaService = marcaService;
		this.grupoService = grupoService;
		this.tamanhoService = tamanhoService;
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
	public String salvar(Model m, RedirectAttributes ra, Produto p) {
		try {
			produtoService.salvarProduto(p);
			String msg = (p.getId() == null) ? "Produto cadastrado com sucesso!"
					: "Produto atualizado com sucesso!";
			showSucesso(ra, msg);
			return REDIRECT;
		} catch (Exception e) {
			e.printStackTrace();
			showError(m, e.getMessage());
			carregarPagina(m);
			m.addAttribute("produto", p);
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
	
	@GetMapping("/processar-sku-produtos")
	public String ajustarSku(Model m) {
		try {
			produtoService.processarSkuProdutos();
			return REDIRECT;
		} catch (Exception e) {
			e.printStackTrace();
			showError(m, e.getMessage());
			return PAGINA;
		}
	}

	private void carregarPagina(Model model) {
		model.addAttribute("produtos", produtoService.consultarProduto());
		model.addAttribute("marcas", marcaService.consultar());
		model.addAttribute("grupos", grupoService.consultar());
		model.addAttribute("subgrupos", grupoService.consultarSubgrupos());
		model.addAttribute("tamanhos", tamanhoService.consultar());
	}
}
