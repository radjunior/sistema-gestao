package br.com.gestao.service;

import java.util.List;

import jakarta.persistence.EntityManager;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import br.com.gestao.entity.Perfil;
import br.com.gestao.entity.Usuario;
import br.com.gestao.repository.PerfilRepository;
import br.com.gestao.repository.UsuarioRepository;

@Component
public class BootstrapService implements ApplicationRunner {

	private static final List<String> PERFIS_PADRAO = List.of("ADMIN_SAAS", "ADMIN_EMPRESA", "GERENTE", "OPERADOR");

	private final PerfilRepository perfilRepository;
	private final UsuarioRepository usuarioRepository;
	private final PasswordEncoder passwordEncoder;
	private final EntityManager entityManager;

	@Value("${app.bootstrap.admin-saas.nome:}")
	private String adminSaasNome;

	@Value("${app.bootstrap.admin-saas.usuario:}")
	private String adminSaasUsuario;

	@Value("${app.bootstrap.admin-saas.senha:}")
	private String adminSaasSenha;

	public BootstrapService(PerfilRepository perfilRepository, UsuarioRepository usuarioRepository,
			PasswordEncoder passwordEncoder, EntityManager entityManager) {
		this.perfilRepository = perfilRepository;
		this.usuarioRepository = usuarioRepository;
		this.passwordEncoder = passwordEncoder;
		this.entityManager = entityManager;
	}

	@Override
	@Transactional
	public void run(ApplicationArguments args) {
		for (String nomePerfil : PERFIS_PADRAO) {
			garantirPerfilUnico(nomePerfil);
		}

		criarAdminSaasInicialSeConfigurado();
	}

	private Perfil garantirPerfilUnico(String nomePerfil) {
		List<Perfil> perfis = perfilRepository.findAllByNomeOrderByIdAsc(nomePerfil);
		if (perfis.isEmpty()) {
			Perfil perfil = new Perfil();
			perfil.setNome(nomePerfil);
			return perfilRepository.save(perfil);
		}

		Perfil perfilCanonico = perfis.getFirst();
		if (perfis.size() > 1) {
			for (int i = 1; i < perfis.size(); i++) {
				unificarPerfilDuplicado(perfilCanonico.getId(), perfis.get(i).getId());
			}
		}
		return perfilCanonico;
	}

	private void criarAdminSaasInicialSeConfigurado() {
		if (adminSaasNome.isBlank() || adminSaasUsuario.isBlank() || adminSaasSenha.isBlank()) {
			return;
		}
		if (usuarioRepository.existsByUsuarioIgnoreCase(adminSaasUsuario.trim().toUpperCase())) {
			return;
		}

		Perfil perfilAdminSaas = garantirPerfilUnico("ADMIN_SAAS");
		if (perfilAdminSaas == null) {
			return;
		}

		Usuario usuario = new Usuario();
		usuario.setNomeCompleto(adminSaasNome.trim());
		usuario.setUsuario(adminSaasUsuario.trim().toUpperCase());
		usuario.setSenha(passwordEncoder.encode(adminSaasSenha));
		usuario.setAtivo(true);
		usuario.getPerfis().add(perfilAdminSaas);
		usuarioRepository.save(usuario);
	}

	private void unificarPerfilDuplicado(Long perfilCanonicoId, Long perfilDuplicadoId) {
		entityManager.createNativeQuery("""
			INSERT INTO gestao.usuario_perfil (usuario_id, perfil_id)
			SELECT up.usuario_id, :perfilCanonicoId
			  FROM gestao.usuario_perfil up
			 WHERE up.perfil_id = :perfilDuplicadoId
			   AND NOT EXISTS (
			       SELECT 1
			         FROM gestao.usuario_perfil existente
			        WHERE existente.usuario_id = up.usuario_id
			          AND existente.perfil_id = :perfilCanonicoId
			   )
		""").setParameter("perfilCanonicoId", perfilCanonicoId)
			.setParameter("perfilDuplicadoId", perfilDuplicadoId)
			.executeUpdate();

		entityManager.createNativeQuery("DELETE FROM gestao.usuario_perfil WHERE perfil_id = :perfilDuplicadoId")
				.setParameter("perfilDuplicadoId", perfilDuplicadoId)
				.executeUpdate();

		entityManager.createNativeQuery("DELETE FROM gestao.perfil WHERE id = :perfilDuplicadoId")
				.setParameter("perfilDuplicadoId", perfilDuplicadoId)
				.executeUpdate();
	}
}
