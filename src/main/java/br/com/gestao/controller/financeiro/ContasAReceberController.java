package br.com.gestao.controller.financeiro;

import java.time.LocalDate;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import br.com.gestao.controller.DefaultController;
import br.com.gestao.entity.Parcela;
import br.com.gestao.entity.enums.FormaPagamento;
import br.com.gestao.entity.enums.StatusParcela;
import br.com.gestao.repository.ClienteRepository;
import br.com.gestao.service.ContasAReceberService;
import br.com.gestao.service.ContextoUsuarioService;

@Controller
@RequestMapping("/financeiro/contas-receber")
public class ContasAReceberController extends DefaultController {

	private static final String PAGINA_LISTA = "financeiro/contas-receber";
	private static final String PAGINA_CLIENTE = "financeiro/contas-receber-cliente";

	private final ContasAReceberService contasAReceberService;
	private final ClienteRepository clienteRepository;
	private final ContextoUsuarioService contextoUsuarioService;

	public ContasAReceberController(ContasAReceberService contasAReceberService, ClienteRepository clienteRepository,
			ContextoUsuarioService contextoUsuarioService) {
		this.contasAReceberService = contasAReceberService;
		this.clienteRepository = clienteRepository;
		this.contextoUsuarioService = contextoUsuarioService;
	}

	@GetMapping
	public String listar(Model model,
			@RequestParam(required = false) StatusParcela status,
			@RequestParam(required = false) LocalDate inicio,
			@RequestParam(required = false) LocalDate fim,
			@RequestParam(required = false) Long clienteId) {
		model.addAttribute("indicadores", contasAReceberService.calcularIndicadores());
		model.addAttribute("resumoDevedores", contasAReceberService.consultarResumoDevedores());
		boolean filtroAtivo = status != null || inicio != null || fim != null || clienteId != null;
		if (filtroAtivo) {
			model.addAttribute("parcelasFiltradas", contasAReceberService.filtrar(status, inicio, fim, clienteId));
		}
		model.addAttribute("statusFiltro", status);
		model.addAttribute("inicioFiltro", inicio);
		model.addAttribute("fimFiltro", fim);
		model.addAttribute("clienteIdFiltro", clienteId);
		model.addAttribute("statusDisponiveis", StatusParcela.values());
		model.addAttribute("clientes", clienteRepository.findAllByEmpresaIdOrderByNomeAsc(
				contextoUsuarioService.getEmpresaIdObrigatoria()));
		model.addAttribute("hoje", LocalDate.now());
		return PAGINA_LISTA;
	}

	@GetMapping("/cliente/{clienteId}")
	public String detalharCliente(Model model, @PathVariable Long clienteId) {
		Long empresaId = contextoUsuarioService.getEmpresaIdObrigatoria();
		model.addAttribute("cliente", clienteRepository.findByIdAndEmpresaId(clienteId, empresaId)
				.orElseThrow(() -> new RuntimeException("Cliente nao encontrado")));
		model.addAttribute("parcelas", contasAReceberService.listarParcelasCliente(clienteId));
		model.addAttribute("formasPagamento", FormaPagamento.values());
		model.addAttribute("hoje", LocalDate.now());
		return PAGINA_CLIENTE;
	}

	@PostMapping("/quitar")
	public String quitar(RedirectAttributes redirectAttributes, ContasAReceberService.QuitacaoForm form) {
		try {
			Parcela parcela = contasAReceberService.quitar(form);
			showSucesso(redirectAttributes, "Parcela " + parcela.getNumeroParcela() + "/"
					+ parcela.getTotalParcelas() + " quitada com sucesso!");
			return "redirect:/financeiro/contas-receber/cliente/" + parcela.getCliente().getId();
		} catch (Exception e) {
			showError(redirectAttributes, e.getMessage());
			return "redirect:/financeiro/contas-receber";
		}
	}

	@PostMapping("/recalcular-parcela")
	public String recalcularParcela(RedirectAttributes redirectAttributes, @RequestParam Long parcelaId) {
		try {
			Parcela parcela = contasAReceberService.recalcularJurosParcela(parcelaId);
			showSucesso(redirectAttributes, "Juros recalculados para a parcela " + parcela.getNumeroParcela() + ".");
			return "redirect:/financeiro/contas-receber/cliente/" + parcela.getCliente().getId();
		} catch (Exception e) {
			showError(redirectAttributes, e.getMessage());
			return "redirect:/financeiro/contas-receber";
		}
	}

	@PostMapping("/recalcular-cliente")
	public String recalcularCliente(RedirectAttributes redirectAttributes, @RequestParam Long clienteId) {
		try {
			int qtd = contasAReceberService.recalcularJurosCliente(clienteId);
			showSucesso(redirectAttributes, qtd + " parcela(s) recalculada(s).");
		} catch (Exception e) {
			showError(redirectAttributes, e.getMessage());
		}
		return "redirect:/financeiro/contas-receber/cliente/" + clienteId;
	}

}
