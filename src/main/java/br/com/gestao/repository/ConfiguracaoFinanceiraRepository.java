package br.com.gestao.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.com.gestao.entity.ConfiguracaoFinanceira;

@Repository
public interface ConfiguracaoFinanceiraRepository extends JpaRepository<ConfiguracaoFinanceira, Long> {

	Optional<ConfiguracaoFinanceira> findByEmpresaId(Long empresaId);

}
