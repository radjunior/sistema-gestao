package br.com.gestao.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.com.gestao.entity.Fornecedor;

@Repository
public interface FornecedorRepository extends JpaRepository<Fornecedor, Long> {

	List<Fornecedor> findAllByEmpresaIdOrderByNomeAsc(Long empresaId);

	Optional<Fornecedor> findByIdAndEmpresaId(Long id, Long empresaId);

	boolean existsByCnpjAndEmpresaId(String cnpj, Long empresaId);

	boolean existsByCnpjAndEmpresaIdAndIdNot(String cnpj, Long empresaId, Long id);

}
