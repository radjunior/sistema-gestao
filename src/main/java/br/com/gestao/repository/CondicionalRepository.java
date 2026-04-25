package br.com.gestao.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import br.com.gestao.entity.Condicional;
import br.com.gestao.entity.enums.StatusCondicional;

@Repository
public interface CondicionalRepository extends JpaRepository<Condicional, Long> {

	Optional<Condicional> findByIdAndEmpresaId(Long id, Long empresaId);

	List<Condicional> findAllByEmpresaIdOrderByDataSaidaDesc(Long empresaId);

	List<Condicional> findAllByEmpresaIdAndStatusOrderByDataSaidaDesc(Long empresaId, StatusCondicional status);

	List<Condicional> findAllByEmpresaIdAndClienteIdOrderByDataSaidaDesc(Long empresaId, Long clienteId);

	@Query("SELECT c FROM Condicional c WHERE c.empresa.id = :empresaId "
			+ "AND (:status IS NULL OR c.status = :status) "
			+ "AND (:clienteId IS NULL OR c.cliente.id = :clienteId) "
			+ "AND (:inicio IS NULL OR c.dataSaida >= :inicio) "
			+ "AND (:fim IS NULL OR c.dataSaida <= :fim) "
			+ "ORDER BY c.dataSaida DESC")
	List<Condicional> filtrar(@Param("empresaId") Long empresaId,
			@Param("status") StatusCondicional status,
			@Param("clienteId") Long clienteId,
			@Param("inicio") java.time.LocalDateTime inicio,
			@Param("fim") java.time.LocalDateTime fim);

	@Query("SELECT c FROM Condicional c WHERE c.status IN :statuses "
			+ "AND c.dataPrevistaDevolucao < :hoje")
	List<Condicional> findVencidasGlobal(@Param("statuses") List<StatusCondicional> statuses,
			@Param("hoje") LocalDate hoje);

}
