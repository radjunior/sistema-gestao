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
import br.com.gestao.entity.TituloAPagar;
import br.com.gestao.entity.enums.FormaPagamento;
import br.com.gestao.entity.enums.StatusParcela;
import br.com.gestao.repository.FornecedorRepository;
import br.com.gestao.service.ContasAPagarService;
import br.com.gestao.service.ContextoUsuarioService;

@Controller
@RequestMapping("/financeiro/contas-pagar")
public class ContasAPagarController extends DefaultController {

	private static final String PAGINA_LISTA = "financeiro/contas-pagar";
	private static final String PAGINA_FORNECEDOR = "financeiro/contas-pagar-fornecedor";

	private final ContasAPagarService contasAPagarService;
	private final FornecedorRepository fornecedorRepository;
	private final ContextoUsuarioService contextoUsuarioService;

	public ContasAPagarController(ContasAPagarService contasAPagarService,
			FornecedorRepository fornecedorRepository,
			ContextoUsuarioService contextoUsuarioService) {
		this.contasAPagarService = contasAPagarService;
		this.fornecedorRepository = fornecedorRepository;
		this.contextoUsuarioService = contextoUsuarioService;
	}

	@GetMapping
	public String listar(Model model,
			@RequestParam(required = false) StatusParcela status,
			@RequestParam(required = false) LocalDate inicio,
			@RequestParam(required = false) LocalDate fim,
			@RequestParam(required = false) Long fornecedorId) {
		model.addAttribute("indicadores", contasAPagarService.calcularIndicadores());
		model.addAttribute("resumoCredores", contasAPagarService.consultarResumoCredores());
		boolean filtroAtivo = status != null || inicio != null || fim != null || fornecedorId != null;
		if (filtroAtivo) {
			model.addAttribute("titulosFiltrados", contasAPagarService.filtrar(status, inicio, fim, fornecedorId));
		}
		model.addAttribute("statusFiltro", status);
		model.addAttribute("inicioFiltro", inicio);
		model.addAttribute("fimFiltro", fim);
		model.addAttribute("fornecedorIdFiltro", fornecedorId);
		model.addAttribute("statusDisponiveis", StatusParcela.values());
		model.addAttribute("fornecedores", fornecedorRepository.findAllByEmpresaIdOrderByNomeAsc(
				contextoUsuarioService.getEmpresaIdObrigatoria()));
		model.addAttribute("hoje", LocalDate.now());
		return PAGINA_LISTA;
	}

	@GetMapping("/fornecedor/{fornecedorId}")
	public String detalharFornecedor(Model model, @PathVariable Long fornecedorId) {
		Long empresaId = contextoUsuarioService.getEmpresaIdObrigatoria();
		model.addAttribute("fornecedor", fornecedorRepository.findByIdAndEmpresaId(fornecedorId, empresaId)
				.orElseThrow(() -> new RuntimeException("Fornecedor não encontrado")));
		model.addAttribute("titulos", contasAPagarService.listarTitulosFornecedor(fornecedorId));
		model.addAttribute("formasPagamento", FormaPagamento.values());
		model.addAttribute("hoje", LocalDate.now());
		return PAGINA_FORNECEDOR;
	}

	@PostMapping("/quitar")
	public String quitar(RedirectAttributes redirectAttributes, ContasAPagarService.QuitacaoTituloForm form) {
		try {
			TituloAPagar titulo = contasAPagarService.quitar(form);
			showSucesso(redirectAttributes, "Título " + titulo.getNumeroParcela() + "/"
					+ titulo.getTotalParcelas() + " quitado com sucesso!");
			Long fornId = titulo.getFornecedor() != null ? titulo.getFornecedor().getId() : null;
			return fornId != null
					? "redirect:/financeiro/contas-pagar/fornecedor/" + fornId
					: "redirect:/financeiro/contas-pagar";
		} catch (Exception e) {
			showError(redirectAttributes, e.getMessage());
			return "redirect:/financeiro/contas-pagar";
		}
	}

	@PostMapping("/recalcular-titulo")
	public String recalcularTitulo(RedirectAttributes redirectAttributes, @RequestParam Long tituloId) {
		try {
			TituloAPagar titulo = contasAPagarService.recalcularJurosTitulo(tituloId);
			showSucesso(redirectAttributes, "Juros recalculados para o título " + titulo.getNumeroParcela() + ".");
			Long fornId = titulo.getFornecedor() != null ? titulo.getFornecedor().getId() : null;
			return fornId != null
					? "redirect:/financeiro/contas-pagar/fornecedor/" + fornId
					: "redirect:/financeiro/contas-pagar";
		} catch (Exception e) {
			showError(redirectAttributes, e.getMessage());
			return "redirect:/financeiro/contas-pagar";
		}
	}

	@PostMapping("/recalcular-fornecedor")
	public String recalcularFornecedor(RedirectAttributes redirectAttributes, @RequestParam Long fornecedorId) {
		try {
			int qtd = contasAPagarService.recalcularJurosFornecedor(fornecedorId);
			showSucesso(redirectAttributes, qtd + " título(s) recalculado(s).");
		} catch (Exception e) {
			showError(redirectAttributes, e.getMessage());
		}
		return "redirect:/financeiro/contas-pagar/fornecedor/" + fornecedorId;
	}

}
