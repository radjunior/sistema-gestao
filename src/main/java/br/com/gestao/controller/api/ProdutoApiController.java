package br.com.gestao.controller.api;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.gestao.entity.Produto;
import br.com.gestao.repository.ProdutoRepository;
import br.com.gestao.service.ContextoUsuarioService;

/**
 * Endpoints JSON compartilhados de busca de produto, usados por todos os
 * campos de pesquisa de produto da aplicacao (PDV, condicional, etc.),
 * inclusive a resolucao por codigo de barras bipado.
 */
@RestController
@RequestMapping("/api/produto")
public class ProdutoApiController {

	public record ProdutoBuscaDTO(Long id, String descricao, String sku, String codigoBarra,
			BigDecimal preco, Integer estoque) {
	}

	private final ProdutoRepository produtoRepository;
	private final ContextoUsuarioService contextoUsuarioService;

	public ProdutoApiController(ProdutoRepository produtoRepository,
			ContextoUsuarioService contextoUsuarioService) {
		this.produtoRepository = produtoRepository;
		this.contextoUsuarioService = contextoUsuarioService;
	}

	private ProdutoBuscaDTO toDto(Produto p) {
		return new ProdutoBuscaDTO(
				p.getId(),
				p.getDescricao(),
				p.getSku(),
				p.getCodigoBarra(),
				p.getPreco(),
				p.getEstoque() != null ? p.getEstoque().getQuantidade() : 0);
	}

	@GetMapping("/buscar")
	public List<ProdutoBuscaDTO> buscar(@RequestParam(required = false, defaultValue = "") String q) {
		String termo = q == null ? "" : q.trim();
		if (termo.isEmpty()) {
			return List.of();
		}
		Long empresaId = contextoUsuarioService.getEmpresaIdObrigatoria();
		return produtoRepository.buscarAtivosPorTermo(empresaId, termo, PageRequest.of(0, 20))
				.stream().map(this::toDto).toList();
	}

	@GetMapping("/por-codigo-barra")
	public ResponseEntity<ProdutoBuscaDTO> porCodigoBarra(@RequestParam String codigo) {
		String cb = codigo == null ? "" : codigo.trim();
		if (cb.isEmpty()) {
			return ResponseEntity.notFound().build();
		}
		Long empresaId = contextoUsuarioService.getEmpresaIdObrigatoria();
		return produtoRepository.findFirstByEmpresaIdAndCodigoBarraAndAtivoTrue(empresaId, cb)
				.map(this::toDto)
				.map(ResponseEntity::ok)
				.orElseGet(() -> ResponseEntity.notFound().build());
	}
}
