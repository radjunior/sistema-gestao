// Gestao de Estoque: modo individual x em massa, filtro e botoes +/-.

(function () {
	const tabela = document.getElementById("tb-estoque");
	if (!tabela) return;

	const filtro = document.getElementById("filtro-produto");
	const chkTodos = document.getElementById("chk-todos");
	const modoIndividual = document.getElementById("modo-individual");
	const modoMassa = document.getElementById("modo-massa");
	const areaMassa = document.getElementById("area-massa");
	const infoInd = document.getElementById("info-modo-individual");
	const infoMassa = document.getElementById("info-modo-massa");
	const formMassa = document.getElementById("form-massa");
	const btnAplicarMassa = document.getElementById("btn-aplicar-massa");
	const csrfToken = document.querySelector('meta[name="_csrf"]')?.getAttribute("content");
	const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.getAttribute("content");

	function criarFormularioPost(action, campos) {
		const form = document.createElement("form");
		form.method = "POST";
		form.action = action;
		form.style.display = "none";

		campos.forEach(({ nome, valor }) => {
			const input = document.createElement("input");
			input.type = "hidden";
			input.name = nome;
			input.value = valor;
			form.appendChild(input);
		});

		if (csrfToken && csrfHeader) {
			const csrfInput = document.createElement("input");
			csrfInput.type = "hidden";
			csrfInput.name = "_csrf";
			csrfInput.value = csrfToken;
			form.appendChild(csrfInput);
		}

		document.body.appendChild(form);
		form.submit();
	}

	// Filtro client-side
	function aplicarFiltro() {
		const termo = (filtro.value || "").trim().toLowerCase();
		tabela.querySelectorAll("tbody tr").forEach(tr => {
			const d = tr.dataset.descricao || "";
			const s = tr.dataset.sku || "";
			const c = tr.dataset.codigo || "";
			const visivel = !termo || d.includes(termo) || s.includes(termo) || c.includes(termo);
			tr.style.display = visivel ? "" : "none";
		});
	}
	filtro.addEventListener("input", aplicarFiltro);

	// Botoes + / -
	tabela.addEventListener("click", ev => {
		const alvo = ev.target.closest("button");
		if (!alvo) return;
		const tr = alvo.closest("tr");
		if (!tr) return;
		const delta = tr.querySelector(".input-delta");
		if (alvo.classList.contains("btn-incr")) {
			delta.value = (parseInt(delta.value, 10) || 0) + 1;
			sincronizarMassa(tr);
		} else if (alvo.classList.contains("btn-decr")) {
			delta.value = (parseInt(delta.value, 10) || 0) - 1;
			sincronizarMassa(tr);
		} else if (alvo.classList.contains("btn-aplicar-individual")) {
			const v = parseInt(delta.value, 10) || 0;
			if (v === 0) {
				alert("Informe um valor de ajuste diferente de zero.");
				return;
			}
			const motivo = prompt("Motivo do ajuste (opcional):") || "";
			tr.querySelector(".input-motivo").value = motivo;
			criarFormularioPost("/cadastro/estoque/ajuste", [
				{ nome: "produtoId", valor: tr.querySelector(".produto-id").value },
				{ nome: "delta", valor: v },
				{ nome: "motivo", valor: motivo }
			]);
		}
	});

	// Input delta manual sincroniza o campo hidden da massa
	tabela.addEventListener("input", ev => {
		if (ev.target.classList.contains("input-delta")) {
			sincronizarMassa(ev.target.closest("tr"));
		}
	});

	function sincronizarMassa(tr) {
		const delta = parseInt(tr.querySelector(".input-delta").value, 10) || 0;
		tr.querySelector(".hidden-massa-delta").value = delta;
	}

	// Alternancia de modo
	function alternarModo() {
		const massa = modoMassa.checked;
		tabela.querySelectorAll(".col-sel").forEach(el => el.classList.toggle("d-none", !massa));
		tabela.querySelectorAll(".col-acao").forEach(el => el.classList.toggle("d-none", massa));
		areaMassa.classList.toggle("d-none", !massa);
		infoInd.classList.toggle("d-none", massa);
		infoMassa.classList.toggle("d-none", !massa);
		if (!massa) {
			// desabilita campos hidden para nao enviar tudo ao submeter um form individual
			tabela.querySelectorAll(".hidden-massa-id, .hidden-massa-delta").forEach(el => el.disabled = true);
		}
	}
	modoIndividual.addEventListener("change", alternarModo);
	modoMassa.addEventListener("change", alternarModo);

	// Marcar/desmarcar todos
	chkTodos.addEventListener("change", () => {
		tabela.querySelectorAll("tbody tr").forEach(tr => {
			if (tr.style.display === "none") return;
			const chk = tr.querySelector(".chk-linha");
			if (chk) chk.checked = chkTodos.checked;
		});
	});

	// Submissao em massa sem form aninhado
	btnAplicarMassa?.addEventListener("click", () => {
		if (!modoMassa.checked) {
			return;
		}
		const valor = parseInt(document.getElementById("valor-massa").value, 10) || 0;
		if (valor <= 0) {
			alert("Informe uma quantidade maior que zero para aplicar em massa.");
			return;
		}

		const campos = [
			{ nome: "tipoMovimento", valor: document.getElementById("tipo-movimento").value },
			{ nome: "valorMassa", valor },
			{ nome: "motivo", valor: formMassa.querySelector('input[name="motivo"]')?.value || "" }
		];

		let enviados = 0;
		tabela.querySelectorAll("tbody tr").forEach(tr => {
			const chk = tr.querySelector(".chk-linha");
			const elegivel = chk && chk.checked;
			if (!elegivel) return;

			campos.push({ nome: "produtoIds", valor: tr.querySelector(".hidden-massa-id").value });
			enviados++;
		});

		if (enviados === 0) {
			alert("Selecione pelo menos um produto.");
			return;
		}

		criarFormularioPost("/cadastro/estoque/ajuste-massa", campos);
	});

	// Estado inicial
	alternarModo();
})();
