window.confirmarExclusao = window.confirmarExclusao || function(botao) {
	const nome = botao.getAttribute("data-nome");
	return confirm(`Deseja realmente excluir: ${nome}?`);
};

document.addEventListener("DOMContentLoaded", function() {
	inicializarTema();
	inicializarTopbar();
	inicializarSidebar();
	inicializarModalVersoes();
});

function inicializarTema() {
	const root = document.documentElement;
	const botao = document.getElementById("theme-toggle");
	const icone = document.getElementById("theme-toggle-icon");
	const storageKey = "app.theme";

	if (!botao || !icone) {
		return;
	}

	const atualizarIcone = () => {
		const temaAtual = root.getAttribute("data-theme") || "light";
		root.setAttribute("data-bs-theme", temaAtual);
		icone.className = temaAtual === "dark" ? "bi bi-sun-fill" : "bi bi-moon-stars";
		botao.setAttribute("aria-label", temaAtual === "dark" ? "Ativar tema claro" : "Ativar tema escuro");
		botao.setAttribute("title", temaAtual === "dark" ? "Tema claro" : "Tema escuro");
	};

	botao.addEventListener("click", function() {
		const temaAtual = root.getAttribute("data-theme") === "dark" ? "dark" : "light";
		const proximoTema = temaAtual === "dark" ? "light" : "dark";
		root.setAttribute("data-theme", proximoTema);
		localStorage.setItem(storageKey, proximoTema);
		atualizarIcone();
	});

	atualizarIcone();
}

function inicializarTopbar() {
	const titulo = document.getElementById("app-page-title");
	if (!titulo) {
		return;
	}

	titulo.textContent = (document.title || "Painel").trim();
}

function inicializarSidebar() {
	const body = document.body;
	const sidebar = document.getElementById("app-sidebar");
	const backdrop = document.querySelector("[data-sidebar-backdrop]");
	const toggles = document.querySelectorAll("[data-sidebar-toggle]");
	const groupButtons = document.querySelectorAll("[data-sidebar-group-toggle]");

	if (!sidebar) {
		return;
	}

	const desktopMedia = window.matchMedia("(min-width: 768px)");

	const obterModo = () => {
		if (desktopMedia.matches) {
			return "desktop";
		}
		return "mobile";
	};

	let estado = {
		mobile: "closed"
	};

	const marcarLinksAtivos = () => {
		const rotaAtual = window.location.pathname;
		document.querySelectorAll(".app-nav-link[href], .app-subnav__link").forEach((link) => {
			const url = new URL(link.href, window.location.origin);
			const ativo = url.pathname === rotaAtual;
			link.classList.toggle("is-active", ativo);

			if (ativo) {
				const itemPai = link.closest(".app-sidebar__nav > li");
				const botaoGrupo = itemPai?.querySelector("[data-sidebar-group-toggle]");
				if (botaoGrupo) {
					botaoGrupo.setAttribute("aria-expanded", "true");
					itemPai.classList.add("is-open");
				}
			}
		});
	};

	const atualizarAtributos = () => {
		const modo = obterModo();
		const aberto = modo === "mobile" ? body.classList.contains("sidebar-mobile-open") : true;
		sidebar.setAttribute("aria-expanded", String(aberto));
		toggles.forEach((toggle) => {
			toggle.setAttribute("aria-expanded", String(aberto));
			toggle.setAttribute("aria-label", aberto ? "Fechar menu lateral" : "Abrir menu lateral");
		});
	};

	const aplicarEstado = () => {
		const modo = obterModo();
		body.classList.add("sidebar-ready");
		body.classList.remove("sidebar-mobile-open");

		if (modo === "mobile" && estado.mobile === "open") {
			body.classList.add("sidebar-mobile-open");
			backdrop?.removeAttribute("hidden");
		} else if (backdrop) {
			backdrop.setAttribute("hidden", "");
		}

		atualizarAtributos();
	};

	const alternarSidebar = () => {
		if (obterModo() === "mobile") {
			estado.mobile = estado.mobile === "open" ? "closed" : "open";
			aplicarEstado();
		}
	};

	const fecharMobile = () => {
		if (obterModo() !== "mobile") {
			return;
		}
		estado.mobile = "closed";
		aplicarEstado();
	};

	toggles.forEach((toggle) => {
		toggle.addEventListener("click", alternarSidebar);
	});

	backdrop?.addEventListener("click", fecharMobile);

	document.addEventListener("keydown", function(event) {
		if (event.key === "Escape") {
			fecharMobile();
		}
	});

	groupButtons.forEach((button) => {
		button.addEventListener("click", function() {
			const item = button.closest("li");
			const expandido = button.getAttribute("aria-expanded") === "true";
			button.setAttribute("aria-expanded", String(!expandido));
			item?.classList.toggle("is-open", !expandido);
		});
	});

	document.querySelectorAll(".app-subnav__link, .app-nav-link[href]").forEach((link) => {
		link.addEventListener("click", function() {
			fecharMobile();
		});
	});

	const aoAlterar = () => {
		if (obterModo() !== "mobile") {
			estado.mobile = "closed";
		}
		aplicarEstado();
	};

	if (typeof desktopMedia.addEventListener === "function") {
		desktopMedia.addEventListener("change", aoAlterar);
	} else {
		desktopMedia.addListener(aoAlterar);
	}

	marcarLinksAtivos();
	aplicarEstado();
}

function inicializarModalVersoes() {
	const script = document.getElementById("versoes-pendentes-data");
	const modalElement = document.getElementById("modal-versao-sistema");
	const botaoConfirmar = document.getElementById("btn-confirmar-versao");
	const titulo = document.getElementById("versao-sistema-titulo");
	const descricao = document.getElementById("versao-sistema-descricao");

	if (!script || !modalElement || !botaoConfirmar || !titulo || !descricao) {
		return;
	}

	let pendentes;
	try {
		pendentes = JSON.parse(script.textContent || "[]");
	} catch (_error) {
		pendentes = [];
	}

	if (!Array.isArray(pendentes) || pendentes.length === 0) {
		return;
	}

	const token = document.querySelector('meta[name="_csrf"]')?.getAttribute("content");
	const header = document.querySelector('meta[name="_csrf_header"]')?.getAttribute("content");
	const modal = new bootstrap.Modal(modalElement, {
		backdrop: "static",
		keyboard: false
	});

	const exibirAtual = () => {
		const atual = pendentes[0];
		if (!atual) {
			return;
		}
		titulo.textContent = atual.titulo || "Nova versao";
		descricao.textContent = atual.descricao || "";
		modal.show();
	};

	botaoConfirmar.addEventListener("click", async function() {
		const atual = pendentes[0];
		if (!atual) {
			modal.hide();
			return;
		}

		const headers = {
			"Content-Type": "application/json"
		};
		if (token && header) {
			headers[header] = token;
		}

		await fetch(`/versoes/${atual.id}/visualizar`, {
			method: "POST",
			headers
		});

		pendentes.shift();
		modal.hide();

		setTimeout(() => {
			if (pendentes.length > 0) {
				exibirAtual();
			}
		}, 200);
	});

	exibirAtual();
}
