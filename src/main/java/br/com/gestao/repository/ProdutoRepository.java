package br.com.gestao.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.com.gestao.entity.Produto;

@Repository
public interface ProdutoRepository extends JpaRepository<Produto, Long> {

	List<Produto> findAllByEmpresaIdOrderByNomeAsc(Long empresaId);

	Optional<Produto> findByIdAndEmpresaId(Long id, Long empresaId);

	boolean existsByNomeIgnoreCaseAndEmpresaId(String nome, Long empresaId);

	boolean existsByNomeIgnoreCaseAndEmpresaIdAndIdNot(String nome, Long empresaId, Long id);

	boolean existsBySkuIgnoreCaseAndEmpresaId(String sku, Long empresaId);

	boolean existsBySkuIgnoreCaseAndEmpresaIdAndIdNot(String sku, Long empresaId, Long id);

}
