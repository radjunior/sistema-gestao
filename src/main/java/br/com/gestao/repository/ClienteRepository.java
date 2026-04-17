package br.com.gestao.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.com.gestao.entity.Cliente;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {

	List<Cliente> findAllByEmpresaIdOrderByNomeAsc(Long empresaId);

	@Query("""
			SELECT c FROM Cliente c
			WHERE c.empresa.id = :empresaId
			  AND c.ativo = true
			  AND (LOWER(c.nome) LIKE LOWER(CONCAT('%', :termo, '%'))
			       OR REPLACE(REPLACE(REPLACE(c.cpf, '.', ''), '-', ''), '/', '') LIKE CONCAT('%', :termo, '%'))
			ORDER BY c.nome ASC
			""")
	List<Cliente> buscarAtivosPorTermo(@Param("empresaId") Long empresaId,
			@Param("termo") String termo, Pageable pageable);

	Optional<Cliente> findByIdAndEmpresaId(Long id, Long empresaId);

	boolean existsByCpfAndEmpresaId(String cpf, Long empresaId);

	boolean existsByCpfAndEmpresaIdAndIdNot(String cpf, Long empresaId, Long id);

}
