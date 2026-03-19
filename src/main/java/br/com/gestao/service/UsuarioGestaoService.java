package br.com.gestao.service;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.gestao.controller.form.UsuarioCadastroForm;
import br.com.gestao.entity.Empresa;
import br.com.gestao.entity.Perfil;
import br.com.gestao.entity.Usuario;
import br.com.gestao.repository.PerfilRepository;
import br.com.gestao.repository.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;

@Service
public class UsuarioGestaoService {

	private final UsuarioRepository usuarioRepository;
	private final PerfilRepository perfilRepository;
	private final PasswordEncoder passwordEncoder;
	private final ContextoUsuarioService contextoUsuarioService;

	public UsuarioGestaoService(UsuarioRepository usuarioRepository, PerfilRepository perfilRepository,
			PasswordEncoder passwordEncoder, ContextoUsuarioService contextoUsuarioService) {
		this.usuarioRepository = usuarioRepository;
		this.perfilRepository = perfilRepository;
		this.passwordEncoder = passwordEncoder;
		this.contextoUsuarioService = contextoUsuarioService;
	}

	public List<Usuario> consultarUsuariosDaEmpresaLogada() {
		return usuarioRepository.findAllByEmpresaIdOrderByNomeCompletoAsc(contextoUsuarioService.getEmpresaIdObrigatoria());
	}

	public Usuario consultarPorIdDaEmpresa(Long id) {
		return usuarioRepository.findByIdAndEmpresaId(id, contextoUsuarioService.getEmpresaIdObrigatoria())
				.orElseThrow(() -> new EntityNotFoundException("Usuario nao encontrado."));
	}

	public List<Perfil> consultarPerfisDisponiveis() {
		return perfilRepository.findAllByNomeNotOrderByNomeAsc("ADMIN_SAAS");
	}

	@Transactional
	public void salvar(UsuarioCadastroForm form) throws Exception {
		validarFormulario(form);

		Empresa empresa = contextoUsuarioService.getEmpresaObrigatoria();
		Usuario usuario = form.getId() == null ? new Usuario() : consultarPorIdDaEmpresa(form.getId());

		usuario.setNomeCompleto(form.getNomeCompleto().trim());
		usuario.setUsuario(form.getUsuario().trim().toUpperCase());
		usuario.setAtivo(form.isAtivo());
		usuario.setEmpresa(empresa);
		usuario.setPerfis(carregarPerfis(form.getPerfilIds()));

		validarLoginUnico(usuario);
		definirSenha(usuario, form);

		usuarioRepository.save(usuario);
	}

	@Transactional
	public void excluir(Long id) {
		usuarioRepository.delete(consultarPorIdDaEmpresa(id));
	}

	private void validarFormulario(UsuarioCadastroForm form) throws Exception {
		if (form == null) {
			throw new Exception("Formulario invalido.");
		}
		if (form.getNomeCompleto() == null || form.getNomeCompleto().isBlank()) {
			throw new Exception("Nome do usuario e obrigatorio.");
		}
		if (form.getUsuario() == null || form.getUsuario().isBlank()) {
			throw new Exception("Login do usuario e obrigatorio.");
		}
		if (form.getPerfilIds() == null || form.getPerfilIds().isEmpty()) {
			throw new Exception("Selecione ao menos um perfil.");
		}
		if (form.getId() == null && (form.getSenha() == null || form.getSenha().isBlank())) {
			throw new Exception("Senha e obrigatoria para novo usuario.");
		}
		if ((form.getSenha() != null && !form.getSenha().isBlank())
				&& !form.getSenha().equals(form.getConfirmeSenha())) {
			throw new Exception("Confirmacao de senha invalida.");
		}
	}

	private Set<Perfil> carregarPerfis(List<Long> perfilIds) throws Exception {
		List<Perfil> perfis = perfilRepository.findAllById(perfilIds);
		if (perfis.size() != perfilIds.size()) {
			throw new Exception("Perfil informado nao encontrado.");
		}
		boolean contemPerfilAdminSaas = perfis.stream().anyMatch(perfil -> "ADMIN_SAAS".equalsIgnoreCase(perfil.getNome()));
		if (contemPerfilAdminSaas) {
			throw new Exception("Nao e permitido atribuir perfil ADMIN_SAAS nesta tela.");
		}
		return new LinkedHashSet<>(perfis);
	}

	private void validarLoginUnico(Usuario usuario) throws Exception {
		if (usuario.getId() == null) {
			if (usuarioRepository.existsByUsuarioIgnoreCase(usuario.getUsuario())) {
				throw new Exception("Ja existe um usuario com o login informado.");
			}
			return;
		}
		if (usuarioRepository.existsByUsuarioIgnoreCaseAndIdNot(usuario.getUsuario(), usuario.getId())) {
			throw new Exception("Ja existe um usuario com o login informado.");
		}
	}

	private void definirSenha(Usuario usuario, UsuarioCadastroForm form) {
		if (form.getSenha() != null && !form.getSenha().isBlank()) {
			usuario.setSenha(passwordEncoder.encode(form.getSenha()));
		}
	}
}
