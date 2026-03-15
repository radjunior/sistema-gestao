package br.com.gestao.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import br.com.gestao.entity.Produto;

@Repository
public interface ProdutoRepository extends JpaRepository<Produto, Long> {

	boolean existsByNome(String nome);

	@Query("""
			select distinct p
			from Produto p
			left join fetch p.variacoes v
			left join fetch v.estoque
			left join fetch p.marca
			left join fetch p.categoria
			left join fetch p.grupo
			left join fetch p.subgrupo
		""")
	List<Produto> consultarProdutoComVariacoes();

}
