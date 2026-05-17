package br.com.gestao.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import br.com.gestao.entity.DevolucaoItem;

@Repository
public interface DevolucaoItemRepository extends JpaRepository<DevolucaoItem, Long> {

	@Query("SELECT COALESCE(SUM(di.quantidadeDevolvida), 0) FROM DevolucaoItem di "
			+ "WHERE di.vendaItem.id = :vendaItemId")
	int totalDevolvidoPorVendaItem(@Param("vendaItemId") Long vendaItemId);

}
