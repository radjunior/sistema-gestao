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
			// Submete o form individual da linha
			const v = parseInt(delta.value, 10) || 0;
			if (v === 0) {
				alert("Informe um valor de ajuste diferente de zero.");
				return;
			}
			const motivo = prompt("Motivo do ajuste (opcional):") || "";
			tr.querySelector(".input-motivo").value = motivo;
			tr.querySelector(".form-individual").submit();
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

	// Antes de submeter em massa, habilitar os hidden (produtoIds) das linhas marcadas
	formMassa.addEventListener("submit", ev => {
		if (!modoMassa.checked) {
			ev.preventDefault();
			return;
		}
		const valor = parseInt(document.getElementById("valor-massa").value, 10) || 0;
		if (valor <= 0) {
			ev.preventDefault();
			alert("Informe uma quantidade maior que zero para aplicar em massa.");
			return;
		}
		let enviados = 0;
		tabela.querySelectorAll("tbody tr").forEach(tr => {
			const chk = tr.querySelector(".chk-linha");
			const idHidden = tr.querySelector(".hidden-massa-id");
			const deltaHidden = tr.querySelector(".hidden-massa-delta");
			const elegivel = chk && chk.checked;
			idHidden.disabled = !elegivel;
			if (deltaHidden) deltaHidden.disabled = true;
			if (elegivel) enviados++;
		});
		if (enviados === 0) {
			ev.preventDefault();
			alert("Selecione pelo menos um produto.");
		}
	});

	// Estado inicial
	alternarModo();
})();
