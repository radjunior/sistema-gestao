package br.com.gestao.service;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import br.com.gestao.entity.Empresa;
import br.com.gestao.entity.Usuario;

@Service
public class ContextoUsuarioService {

	public Usuario getUsuarioLogado() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || !authentication.isAuthenticated()) {
			return null;
		}

		Object principal = authentication.getPrincipal();
		if (principal instanceof Usuario usuario) {
			return usuario;
		}
		return null;
	}

	public Empresa getEmpresaLogada() {
		Usuario usuario = getUsuarioLogado();
		return usuario != null ? usuario.getEmpresa() : null;
	}

	public Long getEmpresaIdObrigatoria() {
		Empresa empresa = getEmpresaLogada();
		if (empresa == null || empresa.getId() == null) {
			throw new AccessDeniedException("Usuario sem empresa vinculada.");
		}
		return empresa.getId();
	}

	public Empresa getEmpresaObrigatoria() {
		Empresa empresa = getEmpresaLogada();
		if (empresa == null || empresa.getId() == null) {
			throw new AccessDeniedException("Usuario sem empresa vinculada.");
		}
		return empresa;
	}

	public boolean isAdminSaas() {
		Usuario usuario = getUsuarioLogado();
		return usuario != null && usuario.possuiPerfil("ADMIN_SAAS");
	}

	public boolean pertenceAEmpresaLogada(Long empresaId) {
		if (empresaId == null) {
			return false;
		}
		if (isAdminSaas()) {
			return true;
		}
		Empresa empresa = getEmpresaLogada();
		return empresa != null && empresaId.equals(empresa.getId());
	}

	public void validarEmpresa(Long empresaId) {
		if (!pertenceAEmpresaLogada(empresaId)) {
			throw new AccessDeniedException("Registro nao pertence a empresa logada.");
		}
	}
}
