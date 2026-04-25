package br.com.gestao.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import br.com.gestao.entity.Estoque;

@Repository
public interface EstoqueRepository extends JpaRepository<Estoque, Long> {

	Optional<Estoque> findByProdutoId(Long produtoId);

	@Query("SELECT COUNT(e) FROM Estoque e WHERE e.produto.empresa.id = :empresaId AND e.estoqueMinimo > 0 AND e.quantidade < e.estoqueMinimo")
	Long contarAbaixoDoMinimo(@Param("empresaId") Long empresaId);

	@Query("SELECT COUNT(e) FROM Estoque e WHERE e.produto.empresa.id = :empresaId AND e.quantidade = 0")
	Long contarSemEstoque(@Param("empresaId") Long empresaId);

	@Query("SELECT e FROM Estoque e JOIN FETCH e.produto WHERE e.produto.empresa.id = :empresaId AND e.estoqueMinimo > 0 AND e.quantidade < e.estoqueMinimo ORDER BY (e.estoqueMinimo - e.quantidade) DESC")
	List<Estoque> listarCriticos(@Param("empresaId") Long empresaId, Pageable pageable);

}
