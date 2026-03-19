package br.com.gestao.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.com.gestao.entity.Empresa;

@Repository
public interface EmpresaRepository extends JpaRepository<Empresa, Long> {

	List<Empresa> findAllByOrderByNomeFantasiaAsc();

	boolean existsBySlugIgnoreCase(String slug);

	boolean existsBySlugIgnoreCaseAndIdNot(String slug, Long id);

	boolean existsByCnpj(String cnpj);

	boolean existsByCnpjAndIdNot(String cnpj, Long id);
}
