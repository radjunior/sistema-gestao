package br.com.gestao.service;

import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.gestao.controller.form.EmpresaCadastroForm;
import br.com.gestao.entity.Empresa;
import br.com.gestao.entity.Perfil;
import br.com.gestao.entity.Usuario;
import br.com.gestao.repository.EmpresaRepository;
import br.com.gestao.repository.PerfilRepository;
import br.com.gestao.repository.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;

@Service
public class EmpresaService {

	private final EmpresaRepository empresaRepository;
	private final UsuarioRepository usuarioRepository;
	private final PerfilRepository perfilRepository;
	private final PasswordEncoder passwordEncoder;

	public EmpresaService(EmpresaRepository empresaRepository, UsuarioRepository usuarioRepository,
			PerfilRepository perfilRepository, PasswordEncoder passwordEncoder) {
		this.empresaRepository = empresaRepository;
		this.usuarioRepository = usuarioRepository;
		this.perfilRepository = perfilRepository;
		this.passwordEncoder = passwordEncoder;
	}

	public List<Empresa> consultarTodas() {
		return empresaRepository.findAllByOrderByNomeFantasiaAsc();
	}

	public Empresa consultarPorId(Long id) {
		return empresaRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Empresa nao encontrada."));
	}

	@Transactional
	public void salvarComAdministrador(EmpresaCadastroForm form) throws Exception {
		validarFormulario(form);

		Empresa empresa = form.getEmpresaId() == null ? new Empresa() : consultarPorId(form.getEmpresaId());
		empresa.setNomeFantasia(form.getNomeFantasia().trim());
		empresa.setRazaoSocial(textoOuNull(form.getRazaoSocial()));
		empresa.setCnpj(textoOuNull(form.getCnpj()));
		empresa.setEmail(textoOuNull(form.getEmail()));
		empresa.setTelefone(textoOuNull(form.getTelefone()));
		empresa.setSlug(form.getSlug().trim().toLowerCase());
		empresa.setStatus(valorPadrao(form.getStatus(), "ATIVA"));
		empresa.setPlano(valorPadrao(form.getPlano(), "BASICO"));
		empresa.setDataInicio(form.getDataInicio());
		empresa.setDataVencimento(form.getDataVencimento());
		empresa.setAtivo(form.isAtivo());

		validarEmpresa(empresa);
		Empresa empresaSalva = empresaRepository.save(empresa);

		if (form.getEmpresaId() == null) {
			criarAdministradorInicial(empresaSalva, form);
		}
	}

	private void validarFormulario(EmpresaCadastroForm form) throws Exception {
		if (form == null) {
			throw new Exception("Formulario invalido.");
		}
		if (form.getNomeFantasia() == null || form.getNomeFantasia().isBlank()) {
			throw new Exception("Nome fantasia e obrigatorio.");
		}
		if (form.getSlug() == null || form.getSlug().isBlank()) {
			throw new Exception("Slug da empresa e obrigatorio.");
		}

		if (form.getEmpresaId() == null) {
			if (form.getAdminNomeCompleto() == null || form.getAdminNomeCompleto().isBlank()) {
				throw new Exception("Nome do usuario administrador e obrigatorio.");
			}
			if (form.getAdminUsuario() == null || form.getAdminUsuario().isBlank()) {
				throw new Exception("Login do usuario administrador e obrigatorio.");
			}
			if (form.getAdminSenha() == null || form.getAdminSenha().isBlank()) {
				throw new Exception("Senha do usuario administrador e obrigatoria.");
			}
			if (!form.getAdminSenha().equals(form.getAdminConfirmeSenha())) {
				throw new Exception("Confirmacao de senha do administrador invalida.");
			}
		}
	}

	private void validarEmpresa(Empresa empresa) throws Exception {
		if (empresa.getId() == null) {
			if (empresaRepository.existsBySlugIgnoreCase(empresa.getSlug())) {
				throw new Exception("Slug da empresa ja cadastrado.");
			}
			if (empresa.getCnpj() != null && empresaRepository.existsByCnpj(empresa.getCnpj())) {
				throw new Exception("CNPJ da empresa ja cadastrado.");
			}
			return;
		}

		if (empresaRepository.existsBySlugIgnoreCaseAndIdNot(empresa.getSlug(), empresa.getId())) {
			throw new Exception("Slug da empresa ja cadastrado.");
		}
		if (empresa.getCnpj() != null && empresaRepository.existsByCnpjAndIdNot(empresa.getCnpj(), empresa.getId())) {
			throw new Exception("CNPJ da empresa ja cadastrado.");
		}
	}

	private void criarAdministradorInicial(Empresa empresa, EmpresaCadastroForm form) throws Exception {
		String usuarioNormalizado = form.getAdminUsuario().trim().toUpperCase();
		if (usuarioRepository.existsByUsuarioIgnoreCase(usuarioNormalizado)) {
			throw new Exception("Ja existe um usuario com o login informado.");
		}

		Perfil perfilAdminEmpresa = perfilRepository.findFirstByNomeOrderByIdAsc("ADMIN_EMPRESA");
		if (perfilAdminEmpresa == null) {
			throw new EntityNotFoundException("Perfil ADMIN_EMPRESA nao encontrado.");
		}

		Usuario usuario = new Usuario();
		usuario.setNomeCompleto(form.getAdminNomeCompleto().trim());
		usuario.setUsuario(usuarioNormalizado);
		usuario.setSenha(passwordEncoder.encode(form.getAdminSenha()));
		usuario.setAtivo(form.isAdminAtivo());
		usuario.setEmpresa(empresa);
		usuario.getPerfis().add(perfilAdminEmpresa);
		usuarioRepository.save(usuario);
	}

	private String valorPadrao(String valor, String padrao) {
		return (valor == null || valor.isBlank()) ? padrao : valor.trim().toUpperCase();
	}

	private String textoOuNull(String valor) {
		return valor == null || valor.isBlank() ? null : valor.trim();
	}
}
