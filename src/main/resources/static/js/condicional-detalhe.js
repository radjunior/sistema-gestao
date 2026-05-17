import { resolverCodigoBarra } from "./scanner.js";

// Apos abrir uma condicional, abre o cupom para impressao automaticamente.
(function () {
	const params = new URLSearchParams(window.location.search);
	if (params.get("cupom") !== "1") return;
	const btn = document.getElementById("btn-cupom");
	if (btn && btn.href) {
		window.open(btn.href, "_blank");
	}
})();

// Autocomplete + bipagem para adicionar produto numa condicional ja aberta.
(function () {
	const input = document.getElementById("cond-add-busca");
	if (!input) return;

	const hiddenId = document.getElementById("cond-add-produto-id");
	const dropdown = document.getElementById("cond-add-dropdown");
	const btn = document.getElementById("cond-add-btn");
	const info = document.getElementById("cond-add-info");
	const inputQtd = document.getElementById("cond-add-qtd");
	const inputPreco = document.getElementById("cond-add-preco");

	let timer = null;
	let resultados = [];
	let indiceAtivo = -1;

	function fmtBRL(v) {
		return "R$ " + Number(v).toLocaleString("pt-BR", {
			minimumFractionDigits: 2, maximumFractionDigits: 2
		});
	}

	function limpar() {
		hiddenId.value = "";
		btn.disabled = true;
	}

	function esconder() {
		dropdown.classList.add("d-none");
		indiceAtivo = -1;
	}

	function renderizar(lista) {
		resultados = lista;
		indiceAtivo = -1;
		if (!lista.length) {
			dropdown.innerHTML = '<div class="list-group-item text-muted small">Nenhum produto encontrado.</div>';
			dropdown.classList.remove("d-none");
			return;
		}
		dropdown.innerHTML = lista.map((p, i) => `
			<button type="button" class="list-group-item list-group-item-action py-2 item-cond-busca"
					data-indice="${i}">
				<div class="d-flex justify-content-between">
					<div>
						<div class="fw-bold">${p.descricao}</div>
						<div class="small text-muted">SKU: ${p.sku || '-'}${p.codigoBarra ? ' | CB: ' + p.codigoBarra : ''}</div>
					</div>
					<div class="text-end ms-2">
						<div class="fw-bold">${fmtBRL(p.preco)}</div>
						<div class="small text-muted">Estq: ${p.estoque}</div>
					</div>
				</div>
			</button>`).join("");
		dropdown.classList.remove("d-none");
	}

	function selecionar(p) {
		hiddenId.value = p.id;
		input.value = p.descricao;
		btn.disabled = false;
		if (inputPreco && !inputPreco.value) {
			inputPreco.placeholder = "sugerido: " + Number(p.preco).toFixed(2);
		}
		info.textContent = `Selecionado: ${p.descricao} (estoque ${p.estoque})`;
		esconder();
		if (inputQtd) inputQtd.focus();
	}

	async function buscar(termo) {
		try {
			const resp = await fetch(`/api/produto/buscar?q=${encodeURIComponent(termo)}`);
			if (!resp.ok) return;
			renderizar(await resp.json());
		} catch (e) {
			console.error("Erro na busca de produto:", e);
		}
	}

	function destacar(novo) {
		const itens = dropdown.querySelectorAll(".item-cond-busca");
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

	input.addEventListener("keydown", async ev => {
		if (ev.key === "ArrowDown" && !dropdown.classList.contains("d-none") && resultados.length) {
			ev.preventDefault();
			indiceAtivo = Math.min(indiceAtivo + 1, resultados.length - 1);
			destacar(indiceAtivo);
			return;
		}
		if (ev.key === "ArrowUp" && !dropdown.classList.contains("d-none") && resultados.length) {
			ev.preventDefault();
			indiceAtivo = Math.max(indiceAtivo - 1, 0);
			destacar(indiceAtivo);
			return;
		}
		if (ev.key === "Escape") { esconder(); return; }
		if (ev.key !== "Enter") return;

		ev.preventDefault();
		const valor = input.value.trim();
		if (valor.length >= 1) {
			const bipado = await resolverCodigoBarra(valor);
			if (bipado) {
				selecionar(bipado);
				return;
			}
		}
		if (!dropdown.classList.contains("d-none") && resultados.length) {
			const alvo = indiceAtivo >= 0 ? resultados[indiceAtivo] : resultados[0];
			if (alvo) selecionar(alvo);
		} else if (valor.length >= 2) {
			buscar(valor);
		}
	});

	dropdown.addEventListener("click", ev => {
		const b = ev.target.closest(".item-cond-busca");
		if (!b) return;
		const p = resultados[parseInt(b.dataset.indice, 10)];
		if (p) selecionar(p);
	});

	document.addEventListener("click", ev => {
		if (!input.contains(ev.target) && !dropdown.contains(ev.target)) esconder();
	});
})();
