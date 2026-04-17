package br.com.gestao.entity;

import java.time.LocalDateTime;
import java.time.ZoneId;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;

@MappedSuperclass
public abstract class EntityAudit {

	private static final ZoneId ZONE_SP = ZoneId.of("America/Sao_Paulo");

	@Column(name = "criado_em")
	private LocalDateTime criadoEm;

	@Column(name = "modificado_em")
	private LocalDateTime modificadoEm;

	@Column(name = "criado_por")
	private Long criadoPor;

	@Column(name = "modificado_por")
	private Long modificadoPor;

	@PrePersist
	protected void onCreate() {
		LocalDateTime agora = LocalDateTime.now(ZONE_SP);
		Long usuarioId = getUsuarioLogado();

		this.criadoEm = agora;
		this.modificadoEm = agora;
		this.criadoPor = usuarioId;
		this.modificadoPor = usuarioId;
	}

	@PreUpdate
	protected void onUpdate() {
		this.modificadoEm = LocalDateTime.now(ZONE_SP);
		this.modificadoPor = getUsuarioLogado();
	}

	private Long getUsuarioLogado() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		if (authentication == null || !authentication.isAuthenticated()) {
			return null;
		}

		Object principal = authentication.getPrincipal();

		if (principal == null || "anonymousUser".equals(principal)) {
			return null;
		}

		if (principal instanceof Usuario usuario) {
			return usuario.getId();
		}

		return null;
	}

	public LocalDateTime getCriadoEm() {
		return criadoEm;
	}

	public void setCriadoEm(LocalDateTime criadoEm) {
		this.criadoEm = criadoEm;
	}

	public LocalDateTime getModificadoEm() {
		return modificadoEm;
	}

	public void setModificadoEm(LocalDateTime modificadoEm) {
		this.modificadoEm = modificadoEm;
	}

	public Long getCriadoPor() {
		return criadoPor;
	}

	public void setCriadoPor(Long criadoPor) {
		this.criadoPor = criadoPor;
	}

	public Long getModificadoPor() {
		return modificadoPor;
	}

	public void setModificadoPor(Long modificadoPor) {
		this.modificadoPor = modificadoPor;
	}

}
