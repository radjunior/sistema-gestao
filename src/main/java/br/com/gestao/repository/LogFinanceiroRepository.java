package br.com.gestao.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.com.gestao.entity.LogFinanceiro;

@Repository
public interface LogFinanceiroRepository extends JpaRepository<LogFinanceiro, Long> {

	List<LogFinanceiro> findAllByEmpresaIdAndParcelaIdOrderByDataHoraDesc(Long empresaId, Long parcelaId);

	List<LogFinanceiro> findTop100ByEmpresaIdOrderByDataHoraDesc(Long empresaId);

}
