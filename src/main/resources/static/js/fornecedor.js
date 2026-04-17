function mascararCnpj(valor) {
	const d = (valor || "").replace(/\D/g, "").slice(0, 14);
	let r = d;
	if (d.length > 12) {
		r = `${d.slice(0, 2)}.${d.slice(2, 5)}.${d.slice(5, 8)}/${d.slice(8, 12)}-${d.slice(12)}`;
	} else if (d.length > 8) {
		r = `${d.slice(0, 2)}.${d.slice(2, 5)}.${d.slice(5, 8)}/${d.slice(8)}`;
	} else if (d.length > 5) {
		r = `${d.slice(0, 2)}.${d.slice(2, 5)}.${d.slice(5)}`;
	} else if (d.length > 2) {
		r = `${d.slice(0, 2)}.${d.slice(2)}`;
	}
	return r;
}

function mascararTelefone(valor) {
	const d = (valor || "").replace(/\D/g, "").slice(0, 11);
	if (d.length === 0) return "";
	if (d.length <= 2) return `(${d}`;
	if (d.length <= 6) return `(${d.slice(0, 2)}) ${d.slice(2)}`;
	if (d.length <= 10) return `(${d.slice(0, 2)}) ${d.slice(2, 6)}-${d.slice(6)}`;
	return `(${d.slice(0, 2)}) ${d.slice(2, 7)}-${d.slice(7)}`;
}

function aplicarMascara(input, fn) {
	if (!input) return;
	input.value = fn(input.value);
	input.addEventListener("input", () => { input.value = fn(input.value); });
}

aplicarMascara(document.getElementById("cnpj"), mascararCnpj);
aplicarMascara(document.getElementById("telefone"), mascararTelefone);

const filtro = document.getElementById("filtro-nome");
if (filtro) {
	filtro.addEventListener("input", function () {
		const valor = this.value.toLowerCase();
		document.querySelectorAll("#tb-fornecedor tbody tr").forEach(linha => {
			const nome = linha.children[1].textContent.toLowerCase();
			linha.style.display = nome.includes(valor) ? "" : "none";
		});
	});
}

window.confirmarExclusao = function (botao) {
	const nome = botao.getAttribute("data-nome");
	return confirm(`Deseja realmente excluir o fornecedor: ${nome}?`);
};
