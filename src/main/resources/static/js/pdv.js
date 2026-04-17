window.confirmarExclusao = function(botao) {
	const nome = botao.getAttribute("data-nome");
	return confirm(`Deseja realmente excluir: ${nome}?`);
};

// Autocomplete de busca de produto no PDV
(function () {
	const input = document.getElementById("busca-produto");
	if (!input) return;

	const hiddenId = document.getElementById("produto-id-selecionado");
	const dropdown = document.getElementById("busca-produto-dropdown");
	const btnAdicionar = document.getElementById("btn-adicionar-item");
	const info = document.getElementById("busca-produto-info");
	const inputQtd = document.getElementById("input-quantidade");

	let timer = null;
	let ultimosResultados = [];
	let indiceAtivo = -1;

	function fmtBRL(v) {
		return "R$ " + Number(v).toLocaleString("pt-BR", {
			minimumFractionDigits: 2, maximumFractionDigits: 2
		});
	}

	function limparSelecao() {
		hiddenId.value = "";
		btnAdicionar.disabled = true;
	}

	function renderizar(lista) {
		ultimosResultados = lista;
		indiceAtivo = -1;
		if (!lista.length) {
			dropdown.innerHTML = '<div class="list-group-item text-muted small">Nenhum produto encontrado.</div>';
			dropdown.classList.remove("d-none");
			return;
		}
		dropdown.innerHTML = lista.map((p, i) => {
			const estoqueClasse = p.estoque <= 0 ? "text-danger"
				: (p.estoque < 5 ? "text-warning" : "text-success");
			return `
				<button type="button" class="list-group-item list-group-item-action py-2 item-busca"
						data-indice="${i}" data-id="${p.id}">
					<div class="d-flex justify-content-between align-items-start">
						<div>
							<div class="fw-bold">${p.descricao}</div>
							<div class="small text-muted">
								SKU: ${p.sku || '-'}
								${p.codigoBarra ? '| CB: ' + p.codigoBarra : ''}
							</div>
						</div>
						<div class="text-end ms-2">
							<div class="fw-bold">${fmtBRL(p.preco)}</div>
							<div class="small ${estoqueClasse}">Estq: ${p.estoque}</div>
						</div>
					</div>
				</button>`;
		}).join("");
		dropdown.classList.remove("d-none");
	}

	function esconder() {
		dropdown.classList.add("d-none");
		indiceAtivo = -1;
	}

	function selecionar(p) {
		hiddenId.value = p.id;
		input.value = p.descricao;
		btnAdicionar.disabled = false;
		const estoqueClasse = p.estoque <= 0 ? "text-danger"
			: (p.estoque < 5 ? "text-warning" : "text-muted");
		info.innerHTML = `<span class="${estoqueClasse}">` +
			`Preco: ${fmtBRL(p.preco)} | Estoque: ${p.estoque}` +
			`</span>`;
		esconder();
		if (inputQtd) inputQtd.focus();
	}

	async function buscar(termo) {
		try {
			const resp = await fetch(`/venda/pdv/buscar-produto?q=${encodeURIComponent(termo)}`);
			if (!resp.ok) return;
			const dados = await resp.json();
			renderizar(dados);
		} catch (e) {
			console.error("Erro na busca de produto:", e);
		}
	}

	function destacar(novo) {
		const itens = dropdown.querySelectorAll(".item-busca");
		itens.forEach((el, i) => el.classList.toggle("active", i === novo));
		if (novo >= 0 && itens[novo]) itens[novo].scrollIntoView({ block: "nearest" });
	}

	input.addEventListener("input", () => {
		limparSelecao();
		info.innerHTML = "";
		const termo = input.value.trim();
		clearTimeout(timer);
		if (termo.length < 2) {
			esconder();
			return;
		}
		timer = setTimeout(() => buscar(termo), 180);
	});

	input.addEventListener("keydown", ev => {
		if (dropdown.classList.contains("d-none") || !ultimosResultados.length) {
			// Enter sem dropdown: se so ha 1 match exato por codigo de barras, permite busca imediata
			if (ev.key === "Enter" && input.value.trim().length >= 2) {
				ev.preventDefault();
				buscar(input.value.trim());
			}
			return;
		}
		if (ev.key === "ArrowDown") {
			ev.preventDefault();
			indiceAtivo = Math.min(indiceAtivo + 1, ultimosResultados.length - 1);
			destacar(indiceAtivo);
		} else if (ev.key === "ArrowUp") {
			ev.preventDefault();
			indiceAtivo = Math.max(indiceAtivo - 1, 0);
			destacar(indiceAtivo);
		} else if (ev.key === "Enter") {
			ev.preventDefault();
			const alvo = indiceAtivo >= 0 ? ultimosResultados[indiceAtivo] : ultimosResultados[0];
			if (alvo) selecionar(alvo);
		} else if (ev.key === "Escape") {
			esconder();
		}
	});

	dropdown.addEventListener("click", ev => {
		const btn = ev.target.closest(".item-busca");
		if (!btn) return;
		const idx = parseInt(btn.dataset.indice, 10);
		const p = ultimosResultados[idx];
		if (p) selecionar(p);
	});

	// Fecha ao clicar fora
	document.addEventListener("click", ev => {
		if (!input.contains(ev.target) && !dropdown.contains(ev.target)) {
			esconder();
		}
	});
})();

// Autocomplete de busca de cliente no PDV (abertura de venda)
(function () {
	const input = document.getElementById("busca-cliente");
	if (!input) return;

	const hiddenId = document.getElementById("cliente-id-selecionado");
	const dropdown = document.getElementById("busca-cliente-dropdown");
	const info = document.getElementById("busca-cliente-info");

	let timer = null;
	let ultimos = [];
	let indiceAtivo = -1;

	function limpar() {
		hiddenId.value = "";
		info.textContent = "Sem cliente = Consumidor final.";
	}

	function renderizar(lista) {
		ultimos = lista;
		indiceAtivo = -1;
		if (!lista.length) {
			dropdown.innerHTML = '<div class="list-group-item text-muted small">Nenhum cliente encontrado.</div>';
			dropdown.classList.remove("d-none");
			return;
		}
		dropdown.innerHTML = lista.map((c, i) => `
			<button type="button" class="list-group-item list-group-item-action py-2 item-busca-cliente"
					data-indice="${i}">
				<div class="fw-bold">${c.nome}</div>
				<div class="small text-muted">
					${c.cpf ? 'CPF: ' + c.cpf : ''} ${c.telefone ? '| Tel: ' + c.telefone : ''}
				</div>
			</button>`).join("");
		dropdown.classList.remove("d-none");
	}

	function esconder() {
		dropdown.classList.add("d-none");
		indiceAtivo = -1;
	}

	function selecionar(c) {
		hiddenId.value = c.id;
		input.value = c.nome;
		info.textContent = `Cliente selecionado: ${c.nome}`;
		esconder();
	}

	async function buscar(termo) {
		try {
			const resp = await fetch(`/venda/pdv/buscar-cliente?q=${encodeURIComponent(termo)}`);
			if (!resp.ok) return;
			renderizar(await resp.json());
		} catch (e) {
			console.error("Erro na busca de cliente:", e);
		}
	}

	function destacar(novo) {
		const itens = dropdown.querySelectorAll(".item-busca-cliente");
		itens.forEach((el, i) => el.classList.toggle("active", i === novo));
		if (novo >= 0 && itens[novo]) itens[novo].scrollIntoView({ block: "nearest" });
	}

	input.addEventListener("input", () => {
		limpar();
		const termo = input.value.trim();
		clearTimeout(timer);
		if (termo.length < 2) { esconder(); return; }
		timer = setTimeout(() => buscar(termo), 180);
	});

	input.addEventListener("keydown", ev => {
		if (dropdown.classList.contains("d-none") || !ultimos.length) return;
		if (ev.key === "ArrowDown") {
			ev.preventDefault();
			indiceAtivo = Math.min(indiceAtivo + 1, ultimos.length - 1);
			destacar(indiceAtivo);
		} else if (ev.key === "ArrowUp") {
			ev.preventDefault();
			indiceAtivo = Math.max(indiceAtivo - 1, 0);
			destacar(indiceAtivo);
		} else if (ev.key === "Enter") {
			ev.preventDefault();
			const alvo = indiceAtivo >= 0 ? ultimos[indiceAtivo] : ultimos[0];
			if (alvo) selecionar(alvo);
		} else if (ev.key === "Escape") {
			esconder();
		}
	});

	dropdown.addEventListener("click", ev => {
		const btn = ev.target.closest(".item-busca-cliente");
		if (!btn) return;
		const idx = parseInt(btn.dataset.indice, 10);
		const c = ultimos[idx];
		if (c) selecionar(c);
	});

	document.addEventListener("click", ev => {
		if (!input.contains(ev.target) && !dropdown.contains(ev.target)) esconder();
	});
})();

// Preview do parcelamento (crediario)
(function () {
	const preview = document.getElementById("parc-preview");
	if (!preview) return;

	const inputTotal = document.getElementById("parc-total");
	const inputTaxa = document.getElementById("parc-taxa");
	const chkJuros = document.getElementById("parc-com-juros");
	const destino = document.getElementById("parc-preview-valor");
	const saldo = parseFloat(preview.dataset.saldo || "0");
	const defaultTaxa = parseFloat(preview.dataset.defaultTaxa || "0");

	function fmt(v) {
		return v.toLocaleString("pt-BR", { minimumFractionDigits: 2, maximumFractionDigits: 2 });
	}

	function calcular() {
		const n = parseInt(inputTotal.value, 10) || 1;
		const comJuros = chkJuros.checked;
		let parcela, total;

		if (!comJuros || saldo <= 0 || n <= 0) {
			parcela = n > 0 ? saldo / n : 0;
			total = saldo;
			destino.innerHTML = `${n}x de <span class="fw-bold">R$ ${fmt(parcela)}</span> sem juros`;
			return;
		}

		let taxaPct = parseFloat(inputTaxa.value);
		if (isNaN(taxaPct)) taxaPct = defaultTaxa;
		const i = taxaPct / 100;

		if (i === 0) {
			parcela = saldo / n;
			total = saldo;
		} else {
			// PMT = PV * i / (1 - (1+i)^-n)
			parcela = saldo * i / (1 - Math.pow(1 + i, -n));
			total = parcela * n;
		}

		destino.innerHTML =
			`${n}x de <span class="fw-bold">R$ ${fmt(parcela)}</span> ` +
			`(total: R$ ${fmt(total)}, juros: R$ ${fmt(total - saldo)})`;
	}

	[inputTotal, inputTaxa, chkJuros].forEach(el => {
		if (el) el.addEventListener("input", calcular);
		if (el) el.addEventListener("change", calcular);
	});
	calcular();
})();
