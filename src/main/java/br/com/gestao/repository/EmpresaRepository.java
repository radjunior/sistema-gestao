package br.com.gestao.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import br.com.gestao.entity.Empresa;

@Repository
public interface EmpresaRepository extends JpaRepository<Empresa, Long> {

	List<Empresa> findAllByOrderByNomeFantasiaAsc();

	boolean existsBySlugIgnoreCase(String slug);

	boolean existsBySlugIgnoreCaseAndIdNot(String slug, Long id);

	boolean existsByCnpj(String cnpj);

	boolean existsByCnpjAndIdNot(String cnpj, Long id);

	@Query("SELECT COUNT(e) FROM Empresa e WHERE e.ativo = true")
	Long contarAtivas();

	@Query("SELECT COUNT(e) FROM Empresa e WHERE e.ativo = true AND (e.dataVencimento < CURRENT_DATE OR e.status = 'SUSPENSA')")
	Long contarEmRisco();

	@Query("SELECT e.plano, COUNT(e) FROM Empresa e WHERE e.ativo = true GROUP BY e.plano ORDER BY COUNT(e) DESC")
	List<Object[]> contagemPorPlano();

	List<Empresa> findTop5ByOrderByIdDesc();
}
