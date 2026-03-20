package br.com.gestao.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.com.gestao.entity.UsuarioVisualizacaoVersao;

@Repository
public interface UsuarioVisualizacaoVersaoRepository extends JpaRepository<UsuarioVisualizacaoVersao, Long> {

	boolean existsByUsuarioIdAndVersaoId(Long usuarioId, Long versaoId);

	List<UsuarioVisualizacaoVersao> findByUsuarioIdAndVersaoIdIn(Long usuarioId, Collection<Long> versaoIds);
}
