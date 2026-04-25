package br.com.gestao.controller.condicional;

import java.math.BigDecimal;
import java.time.LocalDate;
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
import br.com.gestao.entity.Cliente;
import br.com.gestao.entity.Condicional;
import br.com.gestao.entity.Produto;
import br.com.gestao.entity.Venda;
import jakarta.persistence.EntityNotFoundException;
import br.com.gestao.entity.enums.StatusCondicional;
import br.com.gestao.repository.ClienteRepository;
import br.com.gestao.repository.ProdutoRepository;
import br.com.gestao.service.CondicionalService;
import br.com.gestao.service.CondicionalService.CondicionalForm;
import br.com.gestao.service.CondicionalService.CondicionalItemForm;
import br.com.gestao.service.CondicionalService.MovimentoItemForm;
import br.com.gestao.service.ContextoUsuarioService;

@Controller
@RequestMapping("/condicionais")
public class CondicionalController extends DefaultController {

	private static final String PAGINA_LISTA = "condicional/lista";
	private static final String PAGINA_FORM = "condicional/form";
	private static final String PAGINA_DETALHE = "condicional/detalhe";
	private static final String PAGINA_TIMELINE_CLIENTE = "condicional/timeline-cliente";
	private static final String PAGINA_TIMELINE_PRODUTO = "condicional/timeline-produto";

	private final CondicionalService condicionalService;
	private final ClienteRepository clienteRepository;
	private final ProdutoRepository produtoRepository;
	private final ContextoUsuarioService contextoUsuarioService;

	public CondicionalController(CondicionalService condicionalService,
			ClienteRepository clienteRepository,
			ProdutoRepository produtoRepository,
			ContextoUsuarioService contextoUsuarioService) {
		this.condicionalService = condicionalService;
		this.clienteRepository = clienteRepository;
		this.produtoRepository = produtoRepository;
		this.contextoUsuarioService = contextoUsuarioService;
	}

	@GetMapping
	public String listar(Model model,
			@RequestParam(required = false) StatusCondicional status,
			@RequestParam(required = false) Long clienteId,
			@RequestParam(required = false) LocalDate inicio,
			@RequestParam(required = false) LocalDate fim) {
		Long empresaId = contextoUsuarioService.getEmpresaIdObrigatoria();
		boolean filtroAtivo = status != null || clienteId != null || inicio != null || fim != null;
		List<Condicional> lista = filtroAtivo
				? condicionalService.filtrar(status, clienteId, inicio, fim)
				: condicionalService.listar();
		model.addAttribute("condicionais", lista);
		model.addAttribute("indicadores", condicionalService.calcularIndicadores());
		model.addAttribute("statusDisponiveis", StatusCondicional.values());
		model.addAttribute("statusFiltro", status);
		model.addAttribute("clienteIdFiltro", clienteId);
		model.addAttribute("inicioFiltro", inicio);
		model.addAttribute("fimFiltro", fim);
		model.addAttribute("clientes", clienteRepository.findAllByEmpresaIdOrderByNomeAsc(empresaId));
		model.addAttribute("produtos", produtoRepository.findAllByEmpresaIdOrderByDescricaoAsc(empresaId));
		model.addAttribute("hoje", LocalDate.now());
		return PAGINA_LISTA;
	}

	@GetMapping("/nova")
	public String novaForm(Model model) {
		Long empresaId = contextoUsuarioService.getEmpresaIdObrigatoria();
		model.addAttribute("clientes", clienteRepository.findAllByEmpresaIdOrderByNomeAsc(empresaId));
		model.addAttribute("produtos", produtoRepository.findAllByEmpresaIdOrderByDescricaoAsc(empresaId));
		model.addAttribute("dataPrevistaPadrao",
				LocalDate.now().plusDays(CondicionalService.PRAZO_PADRAO_DIAS));
		return PAGINA_FORM;
	}

	@PostMapping
	public String abrir(RedirectAttributes redirectAttributes,
			@RequestParam Long clienteId,
			@RequestParam LocalDate dataPrevistaDevolucao,
			@RequestParam(required = false) String observacao,
			@RequestParam(name = "produtoId", required = false) List<Long> produtoIds,
			@RequestParam(name = "quantidade", required = false) List<Integer> quantidades,
			@RequestParam(name = "precoUnitario", required = false) List<BigDecimal> precos) {
		try {
			CondicionalForm form = new CondicionalForm();
			form.setClienteId(clienteId);
			form.setDataPrevistaDevolucao(dataPrevistaDevolucao);
			form.setObservacao(observacao);

			List<CondicionalItemForm> itens = new ArrayList<>();
			if (produtoIds != null) {
				for (int i = 0; i < produtoIds.size(); i++) {
					Long pid = produtoIds.get(i);
					Integer qtd = quantidades != null && i < quantidades.size() ? quantidades.get(i) : null;
					BigDecimal preco = precos != null && i < precos.size() ? precos.get(i) : null;
					if (pid == null || qtd == null || qtd <= 0) {
						continue;
					}
					CondicionalItemForm it = new CondicionalItemForm();
					it.setProdutoId(pid);
					it.setQuantidade(qtd);
					it.setPrecoUnitario(preco);
					itens.add(it);
				}
			}
			form.setItens(itens);

			Condicional salva = condicionalService.abrir(form);
			showSucesso(redirectAttributes, "Condicional #" + salva.getId() + " aberta com sucesso.");
			return "redirect:/condicionais/" + salva.getId();
		} catch (Exception e) {
			showError(redirectAttributes, e.getMessage());
			return "redirect:/condicionais/nova";
		}
	}

	@GetMapping("/{id}")
	public String detalhar(Model model, @PathVariable Long id) {
		Condicional cond = condicionalService.consultarPorId(id);
		model.addAttribute("condicional", cond);
		model.addAttribute("hoje", LocalDate.now());
		return PAGINA_DETALHE;
	}

	@PostMapping("/{id}/devolver")
	public String devolver(RedirectAttributes redirectAttributes, @PathVariable Long id,
			@RequestParam(name = "itemId", required = false) List<Long> itemIds,
			@RequestParam(name = "quantidade", required = false) List<Integer> quantidades) {
		try {
			List<MovimentoItemForm> movs = montarMovimentos(itemIds, quantidades, null);
			condicionalService.devolverItens(id, movs);
			showSucesso(redirectAttributes, "Devolução registrada com sucesso.");
		} catch (Exception e) {
			showError(redirectAttributes, e.getMessage());
		}
		return "redirect:/condicionais/" + id;
	}

	@PostMapping("/{id}/converter")
	public String converter(RedirectAttributes redirectAttributes, @PathVariable Long id,
			@RequestParam(name = "itemId", required = false) List<Long> itemIds,
			@RequestParam(name = "quantidade", required = false) List<Integer> quantidades,
			@RequestParam(name = "precoUnitarioOverride", required = false) List<BigDecimal> precos) {
		try {
			List<MovimentoItemForm> movs = montarMovimentos(itemIds, quantidades, precos);
			Venda venda = condicionalService.converterEmVenda(id, movs);
			showSucesso(redirectAttributes,
					"Itens convertidos. Venda #" + venda.getId() + " aberta — finalize o pagamento no módulo de Vendas.");
			return "redirect:/condicionais/" + id;
		} catch (Exception e) {
			showError(redirectAttributes, e.getMessage());
			return "redirect:/condicionais/" + id;
		}
	}

	@PostMapping("/{id}/cancelar")
	public String cancelar(RedirectAttributes redirectAttributes, @PathVariable Long id) {
		try {
			condicionalService.cancelar(id);
			showSucesso(redirectAttributes, "Condicional cancelada e itens devolvidos ao estoque.");
		} catch (Exception e) {
			showError(redirectAttributes, e.getMessage());
		}
		return "redirect:/condicionais/" + id;
	}

	@GetMapping("/cliente/{clienteId}")
	public String timelineCliente(Model model, @PathVariable Long clienteId) {
		Long empresaId = contextoUsuarioService.getEmpresaIdObrigatoria();
		Cliente cliente = clienteRepository.findByIdAndEmpresaId(clienteId, empresaId)
				.orElseThrow(() -> new EntityNotFoundException("Cliente não encontrado!"));
		model.addAttribute("cliente", cliente);
		model.addAttribute("itens", condicionalService.timelineCliente(clienteId));
		model.addAttribute("hoje", LocalDate.now());
		return PAGINA_TIMELINE_CLIENTE;
	}

	@GetMapping("/produto/{produtoId}")
	public String timelineProduto(Model model, @PathVariable Long produtoId) {
		Long empresaId = contextoUsuarioService.getEmpresaIdObrigatoria();
		Produto produto = produtoRepository.findByIdAndEmpresaId(produtoId, empresaId)
				.orElseThrow(() -> new EntityNotFoundException("Produto não encontrado!"));
		model.addAttribute("produto", produto);
		model.addAttribute("itens", condicionalService.timelineProduto(produtoId));
		model.addAttribute("hoje", LocalDate.now());
		return PAGINA_TIMELINE_PRODUTO;
	}

	private List<MovimentoItemForm> montarMovimentos(List<Long> itemIds, List<Integer> quantidades,
			List<BigDecimal> precos) {
		List<MovimentoItemForm> movs = new ArrayList<>();
		if (itemIds == null) {
			return movs;
		}
		for (int i = 0; i < itemIds.size(); i++) {
			Long itemId = itemIds.get(i);
			Integer qtd = quantidades != null && i < quantidades.size() ? quantidades.get(i) : null;
			if (itemId == null || qtd == null || qtd <= 0) {
				continue;
			}
			MovimentoItemForm m = new MovimentoItemForm();
			m.setCondicionalItemId(itemId);
			m.setQuantidade(qtd);
			if (precos != null && i < precos.size()) {
				m.setPrecoUnitarioOverride(precos.get(i));
			}
			movs.add(m);
		}
		return movs;
	}

}
