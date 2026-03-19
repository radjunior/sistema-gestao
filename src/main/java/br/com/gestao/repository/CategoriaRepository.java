package br.com.gestao.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.com.gestao.entity.Categoria;

@Repository
public interface CategoriaRepository extends JpaRepository<Categoria, Long> {

	List<Categoria> findAllByEmpresaIdOrderByNomeAsc(Long empresaId);

	Optional<Categoria> findByIdAndEmpresaId(Long id, Long empresaId);

	boolean existsByNomeIgnoreCaseAndEmpresaId(String nome, Long empresaId);

	boolean existsByNomeIgnoreCaseAndEmpresaIdAndIdNot(String nome, Long empresaId, Long id);

}
