// Filtro de produto no select (busca por texto no select)
// Nenhum filtro extra necessario, o select ja mostra info util.

window.confirmarExclusao = function(botao) {
	const nome = botao.getAttribute("data-nome");
	return confirm(`Deseja realmente excluir: ${nome}?`);
}

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
