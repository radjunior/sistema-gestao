window.confirmarExclusao = window.confirmarExclusao || function(botao) {
	const nome = botao.getAttribute("data-nome");
	return confirm(`Deseja realmente excluir: ${nome}?`);
};

document.addEventListener("DOMContentLoaded", function() {
	inicializarModalVersoes();
});

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
