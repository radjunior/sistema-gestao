package br.com.gestao.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.com.gestao.entity.VendaItem;

@Repository
public interface VendaItemRepository extends JpaRepository<VendaItem, Long> {

	List<VendaItem> findAllByVendaId(Long vendaId);

}
