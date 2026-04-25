package br.com.gestao.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import br.com.gestao.entity.LogErroAplicacao;

@Repository
public interface LogErroAplicacaoRepository extends JpaRepository<LogErroAplicacao, Long> {

	List<LogErroAplicacao> findTop200ByOrderByDataHoraDesc();

	List<LogErroAplicacao> findTop200ByClasseExcecaoContainingIgnoreCaseOrderByDataHoraDesc(String classeExcecao);

	List<LogErroAplicacao> findTop200ByMensagemContainingIgnoreCaseOrderByDataHoraDesc(String mensagem);

	@Query("SELECT COUNT(l) FROM LogErroAplicacao l WHERE l.dataHora >= :inicio")
	Long contarApartirDe(@Param("inicio") LocalDateTime inicio);

}
