package br.com.gestao.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.com.gestao.entity.Tamanho;

@Repository
public interface TamanhoRepository extends JpaRepository<Tamanho, Long> {

	List<Tamanho> findAllByEmpresaIdOrderByDescricaoAsc(Long empresaId);

	Optional<Tamanho> findByIdAndEmpresaId(Long id, Long empresaId);

	boolean existsByDescricaoIgnoreCaseAndEmpresaId(String descricao, Long empresaId);

	boolean existsByDescricaoIgnoreCaseAndEmpresaIdAndIdNot(String descricao, Long empresaId, Long id);
}
