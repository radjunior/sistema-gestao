package br.com.gestao.controller.venda;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import br.com.gestao.controller.DefaultController;
import br.com.gestao.entity.ConfiguracaoFinanceira;
import br.com.gestao.entity.Produto;
import br.com.gestao.entity.Venda;
import br.com.gestao.entity.enums.FormaPagamento;
import br.com.gestao.repository.ProdutoRepository;
import br.com.gestao.service.ClienteService;
import br.com.gestao.service.ConfiguracaoFinanceiraService;
import br.com.gestao.service.ContextoUsuarioService;
import br.com.gestao.service.ProdutoService;
import br.com.gestao.service.VendaService;

@Controller
@RequestMapping("/venda/pdv")
public class PdvController extends DefaultController {

	private static final String PAGINA = "venda/pdv";
	private static final String REDIRECT_PDV = "redirect:/venda/pdv/";
	public record ProdutoBuscaDTO(Long id, String descricao, String sku, String codigoBarra,
			BigDecimal preco, Integer estoque) {
	}

	private final VendaService vendaService;
	private final ProdutoService produtoService;
	private final ClienteService clienteService;
	private final ConfiguracaoFinanceiraService configuracaoFinanceiraService;
	private final ProdutoRepository produtoRepository;
	private final ContextoUsuarioService contextoUsuarioService;

	public PdvController(VendaService vendaService, ProdutoService produtoService, ClienteService clienteService,
			ConfiguracaoFinanceiraService configuracaoFinanceiraService,
			ProdutoRepository produtoRepository, ContextoUsuarioService contextoUsuarioService) {
		this.vendaService = vendaService;
		this.produtoService = produtoService;
		this.clienteService = clienteService;
		this.configuracaoFinanceiraService = configuracaoFinanceiraService;
		this.produtoRepository = produtoRepository;
		this.contextoUsuarioService = contextoUsuarioService;
	}

	@GetMapping("/buscar-produto")
	@ResponseBody
	public List<ProdutoBuscaDTO> buscarProduto(@RequestParam(required = false, defaultValue = "") String q) {
		String termo = q == null ? "" : q.trim();
		if (termo.length() < 1) {
			return List.of();
		}
		Long empresaId = contextoUsuarioService.getEmpresaIdObrigatoria();
		List<Produto> produtos = produtoRepository.buscarAtivosPorTermo(empresaId, termo, PageRequest.of(0, 20));
		return produtos.stream()
				.map(p -> new ProdutoBuscaDTO(
						p.getId(),
						p.getDescricao(),
						p.getSku(),
						p.getCodigoBarra(),
						p.getPreco(),
						p.getEstoque() != null ? p.getEstoque().getQuantidade() : 0))
				.toList();
	}

	@GetMapping
	public String novoPdv(Model model) {
		carregarDadosPdv(model, null);
		return PAGINA;
	}

	@GetMapping("/{id}")
	public String abrirPdv(@PathVariable Long id, Model model) {
		try {
			Venda venda = vendaService.consultarPorId(id);
			carregarDadosPdv(model, venda);
		} catch (Exception e) {
			showError(model, e.getMessage());
			carregarDadosPdv(model, null);
		}
		return PAGINA;
	}

	@PostMapping("/abrir")
	public String abrirVenda(RedirectAttributes redirectAttributes,
			@RequestParam(required = false) Long clienteId) {
		try {
			Venda venda = vendaService.abrirVenda(clienteId);
			showSucesso(redirectAttributes, "Venda #" + venda.getId() + " aberta!");
			return REDIRECT_PDV + venda.getId();
		} catch (Exception e) {
			showError(redirectAttributes, e.getMessage());
			return "redirect:/venda/pdv";
		}
	}

	@PostMapping("/{vendaId}/adicionar-item")
	public String adicionarItem(@PathVariable Long vendaId, RedirectAttributes redirectAttributes,
			@RequestParam Long produtoId,
			@RequestParam Integer quantidade,
			@RequestParam(required = false) BigDecimal desconto) {
		try {
			vendaService.adicionarItem(vendaId, produtoId, quantidade, desconto);
			showSucesso(redirectAttributes, "Item adicionado!");
		} catch (Exception e) {
			showError(redirectAttributes, e.getMessage());
		}
		return REDIRECT_PDV + vendaId;
	}

	@PostMapping("/{vendaId}/remover-item")
	public String removerItem(@PathVariable Long vendaId, RedirectAttributes redirectAttributes,
			@RequestParam Long itemId) {
		try {
			vendaService.removerItem(vendaId, itemId);
			showSucesso(redirectAttributes, "Item removido!");
		} catch (Exception e) {
			showError(redirectAttributes, e.getMessage());
		}
		return REDIRECT_PDV + vendaId;
	}

	@PostMapping("/{vendaId}/desconto")
	public String aplicarDesconto(@PathVariable Long vendaId, RedirectAttributes redirectAttributes,
			@RequestParam BigDecimal desconto) {
		try {
			vendaService.aplicarDesconto(vendaId, desconto);
			showSucesso(redirectAttributes, "Desconto aplicado!");
		} catch (Exception e) {
			showError(redirectAttributes, e.getMessage());
		}
		return REDIRECT_PDV + vendaId;
	}

	@PostMapping("/{vendaId}/adicionar-pagamento")
	public String adicionarPagamento(@PathVariable Long vendaId, RedirectAttributes redirectAttributes,
			@RequestParam FormaPagamento formaPagamento,
			@RequestParam BigDecimal valor) {
		try {
			vendaService.adicionarPagamento(vendaId, formaPagamento, valor);
			showSucesso(redirectAttributes, "Pagamento adicionado!");
		} catch (Exception e) {
			showError(redirectAttributes, e.getMessage());
		}
		return REDIRECT_PDV + vendaId;
	}

	@PostMapping("/{vendaId}/remover-pagamento")
	public String removerPagamento(@PathVariable Long vendaId, RedirectAttributes redirectAttributes,
			@RequestParam Long pagamentoId) {
		try {
			vendaService.removerPagamento(vendaId, pagamentoId);
			showSucesso(redirectAttributes, "Pagamento removido!");
		} catch (Exception e) {
			showError(redirectAttributes, e.getMessage());
		}
		return REDIRECT_PDV + vendaId;
	}

	@PostMapping("/{vendaId}/parcelamento")
	public String aplicarParcelamento(@PathVariable Long vendaId, RedirectAttributes redirectAttributes,
			@RequestParam Integer totalParcelas,
			@RequestParam(required = false, defaultValue = "false") Boolean comJuros,
			@RequestParam(required = false) BigDecimal taxaJurosMensal,
			@RequestParam(required = false) Integer diasPrimeiraParcela) {
		try {
			vendaService.aplicarParcelamento(vendaId, totalParcelas, comJuros, taxaJurosMensal, diasPrimeiraParcela);
			showSucesso(redirectAttributes, "Parcelamento configurado: " + totalParcelas + "x!");
		} catch (Exception e) {
			showError(redirectAttributes, e.getMessage());
		}
		return REDIRECT_PDV + vendaId;
	}

	@PostMapping("/{vendaId}/remover-parcelamento")
	public String removerParcelamento(@PathVariable Long vendaId, RedirectAttributes redirectAttributes) {
		try {
			vendaService.removerParcelamento(vendaId);
			showSucesso(redirectAttributes, "Parcelamento removido!");
		} catch (Exception e) {
			showError(redirectAttributes, e.getMessage());
		}
		return REDIRECT_PDV + vendaId;
	}

	@PostMapping("/{vendaId}/finalizar")
	public String finalizarVenda(@PathVariable Long vendaId, RedirectAttributes redirectAttributes) {
		try {
			vendaService.finalizarVenda(vendaId);
			showSucesso(redirectAttributes, "Venda #" + vendaId + " finalizada com sucesso!");
			return "redirect:/venda/vendas";
		} catch (Exception e) {
			showError(redirectAttributes, e.getMessage());
			return REDIRECT_PDV + vendaId;
		}
	}

	@PostMapping("/{vendaId}/cancelar")
	public String cancelarVenda(@PathVariable Long vendaId, RedirectAttributes redirectAttributes) {
		try {
			vendaService.cancelarVenda(vendaId);
			showSucesso(redirectAttributes, "Venda #" + vendaId + " cancelada!");
			return "redirect:/venda/vendas";
		} catch (Exception e) {
			showError(redirectAttributes, e.getMessage());
			return REDIRECT_PDV + vendaId;
		}
	}

	private void carregarDadosPdv(Model model, Venda venda) {
		model.addAttribute("venda", venda);
		model.addAttribute("produtos", produtoService.consultarProduto());
		model.addAttribute("clientes", clienteService.consultar());
		model.addAttribute("formasPagamento", FormaPagamento.values());
		ConfiguracaoFinanceira config = configuracaoFinanceiraService.obterOuCriarPadrao();
		model.addAttribute("configFinanceira", config);
	}

}
