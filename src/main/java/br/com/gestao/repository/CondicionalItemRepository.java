package br.com.gestao.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import br.com.gestao.entity.CondicionalItem;

@Repository
public interface CondicionalItemRepository extends JpaRepository<CondicionalItem, Long> {

	@Query("SELECT i FROM CondicionalItem i "
			+ "WHERE i.condicional.empresa.id = :empresaId "
			+ "AND i.condicional.cliente.id = :clienteId "
			+ "ORDER BY i.condicional.dataSaida DESC, i.id ASC")
	List<CondicionalItem> timelineCliente(@Param("empresaId") Long empresaId,
			@Param("clienteId") Long clienteId);

	@Query("SELECT i FROM CondicionalItem i "
			+ "WHERE i.condicional.empresa.id = :empresaId "
			+ "AND i.produto.id = :produtoId "
			+ "ORDER BY i.condicional.dataSaida DESC, i.id ASC")
	List<CondicionalItem> timelineProduto(@Param("empresaId") Long empresaId,
			@Param("produtoId") Long produtoId);

}
