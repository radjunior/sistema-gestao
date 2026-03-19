package br.com.gestao.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.com.gestao.entity.Subgrupo;

@Repository
public interface SubgrupoRepository extends JpaRepository<Subgrupo, Long>{

	List<Subgrupo> findAllByEmpresaIdOrderByNomeAsc(Long empresaId);

	Optional<Subgrupo> findByIdAndEmpresaId(Long id, Long empresaId);

	boolean existsByNomeIgnoreCaseAndEmpresaId(String nome, Long empresaId);

	boolean existsByNomeIgnoreCaseAndEmpresaIdAndIdNot(String nome, Long empresaId, Long id);

	List<Subgrupo> findByGrupoIdAndEmpresaIdOrderByNomeAsc(Long id, Long empresaId);

}
