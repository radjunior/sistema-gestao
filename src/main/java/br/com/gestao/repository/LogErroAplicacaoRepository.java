package br.com.gestao.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.com.gestao.entity.LogErroAplicacao;

@Repository
public interface LogErroAplicacaoRepository extends JpaRepository<LogErroAplicacao, Long> {

	List<LogErroAplicacao> findTop200ByOrderByDataHoraDesc();

	List<LogErroAplicacao> findTop200ByClasseExcecaoContainingIgnoreCaseOrderByDataHoraDesc(String classeExcecao);

	List<LogErroAplicacao> findTop200ByMensagemContainingIgnoreCaseOrderByDataHoraDesc(String mensagem);
}
