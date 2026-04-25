package br.com.gestao.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import br.com.gestao.entity.VendaItem;

@Repository
public interface VendaItemRepository extends JpaRepository<VendaItem, Long> {

	List<VendaItem> findAllByVendaId(Long vendaId);

	@Query("SELECT vi.produto.descricao, COALESCE(SUM(vi.quantidade), 0), COALESCE(SUM(vi.subtotal), 0) "
			+ "FROM VendaItem vi "
			+ "WHERE vi.venda.empresa.id = :empresaId "
			+ "AND vi.venda.status = br.com.gestao.entity.enums.StatusVenda.FINALIZADA "
			+ "AND vi.venda.dataVenda BETWEEN :inicio AND :fim "
			+ "GROUP BY vi.produto.id, vi.produto.descricao "
			+ "ORDER BY SUM(vi.quantidade) DESC")
	List<Object[]> topProdutosPorQuantidade(@Param("empresaId") Long empresaId, @Param("inicio") LocalDateTime inicio, @Param("fim") LocalDateTime fim, Pageable pageable);

	@Query("SELECT COALESCE(vi.produto.grupo.nome, 'Sem grupo'), COALESCE(SUM(vi.subtotal), 0) "
			+ "FROM VendaItem vi "
			+ "WHERE vi.venda.empresa.id = :empresaId "
			+ "AND vi.venda.status = br.com.gestao.entity.enums.StatusVenda.FINALIZADA "
			+ "AND vi.venda.dataVenda BETWEEN :inicio AND :fim "
			+ "GROUP BY vi.produto.grupo.id, vi.produto.grupo.nome "
			+ "ORDER BY SUM(vi.subtotal) DESC")
	List<Object[]> topGruposPorReceita(@Param("empresaId") Long empresaId, @Param("inicio") LocalDateTime inicio, @Param("fim") LocalDateTime fim, Pageable pageable);

}
