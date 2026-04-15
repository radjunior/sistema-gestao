package br.com.gestao.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.com.gestao.entity.VendaPagamento;

@Repository
public interface VendaPagamentoRepository extends JpaRepository<VendaPagamento, Long> {

	List<VendaPagamento> findAllByVendaId(Long vendaId);

}
