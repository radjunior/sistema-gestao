package br.com.gestao.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import org.springframework.stereotype.Service;

import br.com.gestao.entity.Estoque;
import br.com.gestao.entity.Fornecedor;
import br.com.gestao.entity.Grupo;
import br.com.gestao.entity.Marca;
import br.com.gestao.entity.Produto;
import br.com.gestao.entity.Subgrupo;
import br.com.gestao.entity.Tamanho;
import br.com.gestao.repository.FornecedorRepository;
import br.com.gestao.repository.GrupoRepository;
import br.com.gestao.repository.MarcaRepository;
import br.com.gestao.repository.ProdutoRepository;
import br.com.gestao.repository.SubgrupoRepository;
import br.com.gestao.repository.TamanhoRepository;
import br.com.gestao.util.SkuUtil;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

@Service
public class ProdutoService {

	private final ProdutoRepository produtoRepository;
	private final MarcaRepository marcaRepository;
	private final GrupoRepository grupoRepository;
	private final SubgrupoRepository subgrupoRepository;
	private final TamanhoRepository tamanhoRepository;
	private final FornecedorRepository fornecedorRepository;
	private final ContextoUsuarioService contextoUsuarioService;

	public ProdutoService(ProdutoRepository produtoRepository, MarcaRepository marcaRepository,
			GrupoRepository grupoRepository, SubgrupoRepository subgrupoRepository, TamanhoRepository tamanhoRepository,
			FornecedorRepository fornecedorRepository,
			ContextoUsuarioService contextoUsuarioService) {
		this.produtoRepository = produtoRepository;
		this.marcaRepository = marcaRepository;
		this.grupoRepository = grupoRepository;
		this.subgrupoRepository = subgrupoRepository;
		this.tamanhoRepository = tamanhoRepository;
		this.fornecedorRepository = fornecedorRepository;
		this.contextoUsuarioService = contextoUsuarioService;
	}

	public Produto consultarProdutoPorId(Long id) {
		return produtoRepository.findByIdAndEmpresaId(id, contextoUsuarioService.getEmpresaIdObrigatoria())
				.orElseThrow(() -> new EntityNotFoundException("Produto nao encontrado"));
	}

	@Transactional
	public void processarSkuProdutos() throws Exception {
		List<Produto> produtos = consultarProduto();
		for (Produto produto : produtos) {
			if (produto.getSku() == null || produto.getSku().isBlank()) {
				produto.setSku(SkuUtil.gerarSku(produto));
				validarSku(produto);
				produtoRepository.save(produto);
			}
		}
	}

	@Transactional
	public void salvarProduto(Produto produto) throws Exception {
		if (produto == null) {
			throw new Exception("Produto invalido");
		}

		Long empresaId = contextoUsuarioService.getEmpresaIdObrigatoria();
		Produto entidade = produto.getId() == null ? new Produto() : consultarProdutoPorId(produto.getId());
		String descricao = validarTextoObrigatorio(produto.getDescricao(), "Descricao invalida");

		validarDescricaoUnica(descricao, produto.getId(), empresaId);

		entidade.setEmpresa(contextoUsuarioService.getEmpresaObrigatoria());
		entidade.setDescricao(descricao);
		entidade.setCodigoBarra(textoOuNull(produto.getCodigoBarra()));
		entidade.setCodigoFabricante(textoOuNull(produto.getCodigoFabricante()));
		entidade.setAtivo(produto.isAtivo());
		entidade.setNcm(textoOuNull(produto.getNcm()));
		entidade.setCusto(validarValor(produto.getCusto(), "Custo nao informado."));
		entidade.setMarca(buscarMarcaDaEmpresa(produto.getMarca(), empresaId));
		entidade.setGrupo(buscarGrupoDaEmpresa(produto.getGrupo(), empresaId));
		entidade.setSubgrupo(buscarSubgrupoDaEmpresa(produto.getSubgrupo(), empresaId));
		entidade.setTamanho(buscarTamanhoDaEmpresa(produto.getTamanho(), empresaId));
		entidade.setFornecedor(buscarFornecedorDaEmpresa(produto.getFornecedor(), empresaId));

		definirPrecoEMargem(entidade, produto.getPreco(), produto.getMargem());
		validarRelacionamentos(entidade);
		validarDadosComerciais(entidade);
		configurarEstoque(entidade, produto.getEstoque());

		String skuInformado = textoOuNull(produto.getSku());
		if (skuInformado != null) {
			entidade.setSku(skuInformado);
			validarSku(entidade);
		}

		Produto produtoSalvo = produtoRepository.save(entidade);

		if (produtoSalvo.getSku() == null || produtoSalvo.getSku().isBlank()) {
			produtoSalvo.setSku(SkuUtil.gerarSku(produtoSalvo));
			validarSku(produtoSalvo);
			produtoRepository.save(produtoSalvo);
		}
	}

	private void definirPrecoEMargem(Produto entidade, BigDecimal precoInformado, BigDecimal margemInformada) throws Exception {
		BigDecimal custo = entidade.getCusto();
		if (precoInformado != null) {
			entidade.setPreco(precoInformado.setScale(2, RoundingMode.HALF_UP));
			entidade.setMargem(calcularMargem(custo, precoInformado));
			return;
		}

		BigDecimal margem = validarValor(margemInformada, "Margem nao informada.");
		entidade.setMargem(margem.setScale(2, RoundingMode.HALF_UP));
		entidade.setPreco(calcularPreco(custo, margem));
	}

	private void validarRelacionamentos(Produto produto) throws Exception {
		if (produto.getMarca() == null || produto.getMarca().getId() == null) {
			throw new Exception("Marca invalida");
		}
		if (produto.getGrupo() == null || produto.getGrupo().getId() == null) {
			throw new Exception("Grupo invalido");
		}
		if (produto.getSubgrupo() == null || produto.getSubgrupo().getId() == null) {
			throw new Exception("Subgrupo invalido");
		}
		if (!produto.getGrupo().getId().equals(produto.getSubgrupo().getGrupo().getId())) {
			throw new Exception("Subgrupo nao pertence ao grupo informado.");
		}
	}

	private void validarDadosComerciais(Produto produto) throws Exception {
		if (produto.getCusto().compareTo(BigDecimal.ZERO) < 0) {
			throw new Exception("Custo nao pode ser negativo.");
		}
		if (produto.getMargem().compareTo(BigDecimal.ZERO) < 0) {
			throw new Exception("Margem nao pode ser negativa.");
		}
		if (produto.getPreco().compareTo(BigDecimal.ZERO) < 0) {
			throw new Exception("Preco nao pode ser negativo.");
		}
		if (produto.getCodigoBarra() != null && produto.getCodigoBarra().length() > 60) {
			throw new Exception("Codigo de barras deve ter no maximo 60 caracteres.");
		}
		if (produto.getCodigoFabricante() != null && produto.getCodigoFabricante().length() > 60) {
			throw new Exception("Codigo do fabricante deve ter no maximo 60 caracteres.");
		}
		if (produto.getSku() != null && produto.getSku().length() > 60) {
			throw new Exception("SKU deve ter no maximo 60 caracteres.");
		}
	}

	private void configurarEstoque(Produto produto, Estoque estoqueEntrada) throws Exception {
		if (estoqueEntrada == null) {
			throw new Exception("Estoque nao informado.");
		}
		if (estoqueEntrada.getQuantidade() == null) {
			throw new Exception("Quantidade em estoque nao informada.");
		}
		if (estoqueEntrada.getEstoqueMinimo() == null) {
			throw new Exception("Estoque minimo nao informado.");
		}
		if (estoqueEntrada.getQuantidade() < 0) {
			throw new Exception("Quantidade em estoque nao pode ser negativa.");
		}
		if (estoqueEntrada.getEstoqueMinimo() < 0) {
			throw new Exception("Estoque minimo nao pode ser negativo.");
		}

		Estoque estoque = produto.getEstoque();
		if (estoque == null) {
			estoque = new Estoque();
			produto.setEstoque(estoque);
		}
		estoque.setQuantidade(estoqueEntrada.getQuantidade());
		estoque.setEstoqueMinimo(estoqueEntrada.getEstoqueMinimo());
		estoque.setProduto(produto);
	}

	private void validarSku(Produto produto) throws Exception {
		Long empresaId = contextoUsuarioService.getEmpresaIdObrigatoria();
		if (produto.getId() == null) {
			if (produtoRepository.existsBySkuIgnoreCaseAndEmpresaId(produto.getSku(), empresaId)) {
				throw new Exception("Ja existe um produto com o SKU informado: " + produto.getSku());
			}
			return;
		}

		if (produtoRepository.existsBySkuIgnoreCaseAndEmpresaIdAndIdNot(produto.getSku(), empresaId, produto.getId())) {
			throw new Exception("Ja existe outro produto com o SKU informado: " + produto.getSku());
		}
	}

	private void validarDescricaoUnica(String descricao, Long id, Long empresaId) throws Exception {
		if (id == null) {
			if (produtoRepository.existsByDescricaoIgnoreCaseAndEmpresaId(descricao, empresaId)) {
				throw new Exception("Ja existe um produto com a descricao informada.");
			}
			return;
		}

		if (produtoRepository.existsByDescricaoIgnoreCaseAndEmpresaIdAndIdNot(descricao, empresaId, id)) {
			throw new Exception("Ja existe outro produto com a descricao informada.");
		}
	}

	private Marca buscarMarcaDaEmpresa(Marca marca, Long empresaId) {
		if (marca == null || marca.getId() == null) {
			return null;
		}
		return marcaRepository.findByIdAndEmpresaId(marca.getId(), empresaId)
				.orElseThrow(() -> new EntityNotFoundException("Marca nao encontrada."));
	}

	private Grupo buscarGrupoDaEmpresa(Grupo grupo, Long empresaId) {
		if (grupo == null || grupo.getId() == null) {
			return null;
		}
		return grupoRepository.findByIdAndEmpresaId(grupo.getId(), empresaId)
				.orElseThrow(() -> new EntityNotFoundException("Grupo nao encontrado."));
	}

	private Subgrupo buscarSubgrupoDaEmpresa(Subgrupo subgrupo, Long empresaId) {
		if (subgrupo == null || subgrupo.getId() == null) {
			return null;
		}
		return subgrupoRepository.findByIdAndEmpresaId(subgrupo.getId(), empresaId)
				.orElseThrow(() -> new EntityNotFoundException("Subgrupo nao encontrado."));
	}

	private Tamanho buscarTamanhoDaEmpresa(Tamanho tamanho, Long empresaId) {
		if (tamanho == null || tamanho.getId() == null) {
			return null;
		}
		return tamanhoRepository.findByIdAndEmpresaId(tamanho.getId(), empresaId)
				.orElseThrow(() -> new EntityNotFoundException("Tamanho nao encontrado."));
	}

	private Fornecedor buscarFornecedorDaEmpresa(Fornecedor fornecedor, Long empresaId) {
		if (fornecedor == null || fornecedor.getId() == null) {
			return null;
		}
		return fornecedorRepository.findByIdAndEmpresaId(fornecedor.getId(), empresaId)
				.orElseThrow(() -> new EntityNotFoundException("Fornecedor nao encontrado."));
	}

	private BigDecimal calcularPreco(BigDecimal custo, BigDecimal margem) {
		return custo.add(custo.multiply(margem).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP)).setScale(2,
				RoundingMode.HALF_UP);
	}

	private BigDecimal calcularMargem(BigDecimal custo, BigDecimal preco) throws Exception {
		if (custo.compareTo(BigDecimal.ZERO) == 0) {
			if (preco.compareTo(BigDecimal.ZERO) > 0) {
				throw new Exception("Nao e possivel informar preco maior que zero com custo zerado.");
			}
			return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
		}

		return preco.subtract(custo).multiply(new BigDecimal("100")).divide(custo, 2, RoundingMode.HALF_UP);
	}

	public void excluirProduto(Produto produto) {
		produtoRepository.delete(consultarProdutoPorId(produto.getId()));
	}

	public List<Produto> consultarProduto() {
		return produtoRepository.findAllByEmpresaIdOrderByDescricaoAsc(contextoUsuarioService.getEmpresaIdObrigatoria());
	}

	private String validarTextoObrigatorio(String valor, String mensagem) throws Exception {
		if (valor == null || valor.isBlank()) {
			throw new Exception(mensagem);
		}
		return valor.trim();
	}

	private String textoOuNull(String valor) {
		return valor == null || valor.isBlank() ? null : valor.trim();
	}

	private BigDecimal validarValor(BigDecimal valor, String mensagem) throws Exception {
		if (valor == null) {
			throw new Exception(mensagem);
		}
		return valor;
	}
}
