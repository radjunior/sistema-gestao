package br.com.gestao.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.com.gestao.entity.MovimentacaoCaixa;

@Repository
public interface MovimentacaoCaixaRepository extends JpaRepository<MovimentacaoCaixa, Long> {

	List<MovimentacaoCaixa> findAllByCaixaIdOrderByDataHoraAsc(Long caixaId);

}
