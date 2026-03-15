package br.com.gestao.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.com.gestao.entity.ProdutoVariacao;

@Repository
public interface ProdutoVariacaoRepository extends JpaRepository<ProdutoVariacao, Long> {

	boolean existsBySku(String sku);

	boolean existsBySkuAndIdNot(String sku, Long id);

	boolean existsByProdutoIdAndCorIgnoreCaseAndTamanhoIgnoreCase(Long produtoId, String cor, String tamanho);

	boolean existsByProdutoIdAndCorIgnoreCaseAndTamanhoIgnoreCaseAndIdNot(Long produtoId, String cor, String tamanho,
			Long id);

}
