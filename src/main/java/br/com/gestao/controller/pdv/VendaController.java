package br.com.gestao.controller.pdv;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import br.com.gestao.controller.DefaultController;
import br.com.gestao.entity.FormaPagamento;
import br.com.gestao.entity.Venda;
import br.com.gestao.repository.ClienteRepository;
import br.com.gestao.repository.ProdutoRepository;
import br.com.gestao.service.ConfiguracaoFinanceiraService;
import br.com.gestao.service.ContextoUsuarioService;
import br.com.gestao.service.VendaService;

@Controller
@RequestMapping("/pdv/venda")
public class VendaController extends DefaultController {

	private static final String PAGINA = "pdv/venda";

	private final VendaService vendaService;
	private final ProdutoRepository produtoRepository;
	private final ClienteRepository clienteRepository;
	private final ConfiguracaoFinanceiraService configuracaoFinanceiraService;
	private final ContextoUsuarioService contextoUsuarioService;

	public VendaController(VendaService vendaService, ProdutoRepository produtoRepository,
			ClienteRepository clienteRepository, ConfiguracaoFinanceiraService configuracaoFinanceiraService,
			ContextoUsuarioService contextoUsuarioService) {
		this.vendaService = vendaService;
		this.produtoRepository = produtoRepository;
		this.clienteRepository = clienteRepository;
		this.configuracaoFinanceiraService = configuracaoFinanceiraService;
		this.contextoUsuarioService = contextoUsuarioService;
	}

	@GetMapping
	public String abrir(Model model) {
		Long empresaId = contextoUsuarioService.getEmpresaIdObrigatoria();
		model.addAttribute("produtos", produtoRepository.findAllByEmpresaIdOrderByDescricaoAsc(empresaId));
		model.addAttribute("clientes", clienteRepository.findAllByEmpresaIdOrderByNomeAsc(empresaId));
		model.addAttribute("formasPagamento", FormaPagamento.values());
		model.addAttribute("config", configuracaoFinanceiraService.obterOuCriarPadrao());
		model.addAttribute("vendasRecentes", vendaService.consultarRecentes());
		return PAGINA;
	}

	@PostMapping("/finalizar")
	@ResponseBody
	public ResponseEntity<Map<String, Object>> finalizar(@RequestBody VendaService.VendaForm form) {
		try {
			Venda venda = vendaService.finalizar(form);
			return ResponseEntity.ok(Map.of(
					"ok", true,
					"vendaId", venda.getId(),
					"mensagem", "Venda #" + venda.getId() + " finalizada com sucesso!"));
		} catch (Exception e) {
			return ResponseEntity.badRequest().body(Map.of("ok", false, "mensagem", e.getMessage()));
		}
	}

}
