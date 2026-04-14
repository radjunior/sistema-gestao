package br.com.gestao.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.com.gestao.entity.Cliente;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {

	List<Cliente> findAllByEmpresaIdOrderByNomeAsc(Long empresaId);

	Optional<Cliente> findByIdAndEmpresaId(Long id, Long empresaId);

	boolean existsByCpfAndEmpresaId(String cpf, Long empresaId);

	boolean existsByCpfAndEmpresaIdAndIdNot(String cpf, Long empresaId, Long id);

}
