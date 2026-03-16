package br.com.gestao.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.com.gestao.entity.Produto;

@Repository
public interface ProdutoRepository extends JpaRepository<Produto, Long> {

	boolean existsByNome(String nome);

	boolean existsBySku(String sku);

	boolean existsBySkuAndIdNot(String sku, Long id);

}
