package br.com.gestao.repository;

import java.util.Optional;
import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import br.com.gestao.entity.Usuario;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

	@EntityGraph(attributePaths = { "empresa", "perfis" })
	Optional<Usuario> findByUsuario(String usuario);

	List<Usuario> findAllByEmpresaIdOrderByNomeCompletoAsc(Long empresaId);

	Optional<Usuario> findByIdAndEmpresaId(Long id, Long empresaId);

	boolean existsByUsuarioIgnoreCase(String usuario);

	boolean existsByUsuarioIgnoreCaseAndIdNot(String usuario, Long id);

	List<Usuario> findAllByOrderByNomeCompletoAsc();

	@Query("SELECT COUNT(u) FROM Usuario u WHERE u.ativo = true AND u.empresa IS NOT NULL")
	Long contarAtivosNaoSaas();

	@Query("SELECT COUNT(u) FROM Usuario u WHERE u.empresa.id = :empresaId AND u.ativo = true")
	Long contarAtivosPorEmpresa(@Param("empresaId") Long empresaId);

}
