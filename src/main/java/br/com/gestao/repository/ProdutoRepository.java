package br.com.gestao.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.com.gestao.entity.Produto;

@Repository
public interface ProdutoRepository extends JpaRepository<Produto, Long> {

	List<Produto> findAllByEmpresaIdOrderByDescricaoAsc(Long empresaId);

	@Query("SELECT p FROM Produto p WHERE p.empresa.id = :empresaId AND p.ativo = true AND ("
			+ "LOWER(p.descricao) LIKE LOWER(CONCAT('%', :termo, '%')) OR "
			+ "LOWER(p.sku) LIKE LOWER(CONCAT('%', :termo, '%')) OR "
			+ "LOWER(COALESCE(p.codigoBarra, '')) LIKE LOWER(CONCAT('%', :termo, '%')) OR "
			+ "LOWER(COALESCE(p.codigoFabricante, '')) LIKE LOWER(CONCAT('%', :termo, '%'))) "
			+ "ORDER BY p.descricao ASC")
	List<Produto> buscarAtivosPorTermo(@Param("empresaId") Long empresaId, @Param("termo") String termo,
			Pageable pageable);

	Optional<Produto> findByIdAndEmpresaId(Long id, Long empresaId);

	boolean existsByDescricaoIgnoreCaseAndEmpresaId(String descricao, Long empresaId);

	boolean existsByDescricaoIgnoreCaseAndEmpresaIdAndIdNot(String descricao, Long empresaId, Long id);

	boolean existsBySkuIgnoreCaseAndEmpresaId(String sku, Long empresaId);

	boolean existsBySkuIgnoreCaseAndEmpresaIdAndIdNot(String sku, Long empresaId, Long id);
}
