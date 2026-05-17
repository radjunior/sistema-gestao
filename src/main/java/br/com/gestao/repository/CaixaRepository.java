package br.com.gestao.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.com.gestao.entity.Caixa;
import br.com.gestao.entity.enums.StatusCaixa;

@Repository
public interface CaixaRepository extends JpaRepository<Caixa, Long> {

	Optional<Caixa> findByIdAndEmpresaId(Long id, Long empresaId);

	Optional<Caixa> findFirstByEmpresaIdAndUsuarioIdAndStatusOrderByDataAberturaDesc(
			Long empresaId, Long usuarioId, StatusCaixa status);

	List<Caixa> findAllByEmpresaIdOrderByDataAberturaDesc(Long empresaId);

}
