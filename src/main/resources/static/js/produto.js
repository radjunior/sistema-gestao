const STORAGE_FORM_CADASTRO = "produto_form_cadastro_estado";
const STORAGE_FORM_VARIACAO = "produto_variacao_form_estado";

document.addEventListener("DOMContentLoaded", function() {
	const inputNome = document.getElementById("nome-produto");
	if (inputNome) {
		inputNome.addEventListener("input", function() {
			const filtro = this.value.toLowerCase();
			const linhas = document.querySelectorAll("#tb-produto tbody tr");

			linhas.forEach(linha => {
				const nome = linha.children[2]?.textContent.toLowerCase() || "";
				linha.style.display = nome.includes(filtro) ? "" : "none";
			});
		});
	}
	aplicarEstadoFormulario();
	aplicarEstadoFormularioVariacao();
	const custoInput = document.getElementById("custo");
	const margemInput = document.getElementById("margem");
	custoInput.addEventListener("input", function() {
		aplicarMascaraMoeda(this);
	});
	margemInput.addEventListener("input", calcularPreco);
});

window.definirFormCadastro = function() {
	const form = document.getElementById("frm-produto");
	const estaRecolhido = form.classList.toggle("recolhido");

	if (estaRecolhido) {
		localStorage.setItem(STORAGE_FORM_CADASTRO, "recolhido");
	} else {
		localStorage.setItem(STORAGE_FORM_CADASTRO, "expandido");
	}

	atualizarIcone();
};

function aplicarEstadoFormulario() {
	const estado = localStorage.getItem(STORAGE_FORM_CADASTRO) || "expandido";
	const form = document.getElementById("frm-produto");

	if (estado === "recolhido") {
		form.classList.add("recolhido");
	} else {
		form.classList.remove("recolhido");
	}

	atualizarIcone();
}

function atualizarIcone() {
	const btnToggle = document.getElementById("btnToggle");
	const form = document.getElementById("frm-produto");
	const recolhido = form.classList.contains("recolhido");

	btnToggle.classList.toggle("bi-arrow-bar-up", !recolhido);
	btnToggle.classList.toggle("bi-arrow-bar-down", recolhido);
}

window.confirmarExclusao = function(botao) {
	const nome = botao.getAttribute("data-nome");
	return confirm(`Deseja realmente excluir: ${nome}?`);
};

window.definirFormCadastroVariacao = function() {
	const form = document.getElementById("frm-produto-variacao");
	const estaRecolhido = form.classList.toggle("recolhido");

	if (estaRecolhido) {
		localStorage.setItem(STORAGE_FORM_VARIACAO, "recolhido");
	} else {
		localStorage.setItem(STORAGE_FORM_VARIACAO, "expandido");
	}

	atualizarIconeVariacao();
};

function aplicarEstadoFormularioVariacao() {
	const estado = localStorage.getItem(STORAGE_FORM_VARIACAO) || "expandido";
	const form = document.getElementById("frm-produto-variacao");

	if (!form) return;

	if (estado === "recolhido") {
		form.classList.add("recolhido");
	} else {
		form.classList.remove("recolhido");
	}

	atualizarIconeVariacao();
}

function atualizarIconeVariacao() {
	const btnToggle = document.getElementById("btnToggleVariacao");
	const form = document.getElementById("frm-produto-variacao");

	if (!btnToggle || !form) return;

	const recolhido = form.classList.contains("recolhido");

	btnToggle.classList.toggle("bi-arrow-bar-up", !recolhido);
	btnToggle.classList.toggle("bi-arrow-bar-down", recolhido);
}

function debounce(fn, delay = 250) {
	let timer;
	return function(...args) {
		clearTimeout(timer);
		timer = setTimeout(() => fn.apply(this, args), delay);
	};
}

function calcularPreco() {
	const custo = parseMoeda(document.getElementById("custo").value);
	const margem = parseFloat(document.getElementById("margem").value) || 0;
	const preco = custo + (custo * margem / 100);
	document.getElementById("preco").value = formatarMoeda(preco);
}

function formatarMoeda(valor) {
	return valor.toLocaleString('pt-BR', {
		minimumFractionDigits: 2,
		maximumFractionDigits: 2
	});
}

function parseMoeda(valor) {
	if (!valor) return 0;
	return parseFloat(valor.replace(/\./g, '').replace(',', '.'));
}

function aplicarMascaraMoeda(input) {
	let numeros = input.value.replace(/\D/g, '');
	if (numeros === '') numeros = '0';
	let valor = parseInt(numeros) / 100;
	input.value = formatarMoeda(valor);
	calcularPreco();
}

window.toggleVariacoes = function(produtoId) {
	const linha = document.getElementById(`variacoes-${produtoId}`);
	const icon = document.getElementById(`icon-${produtoId}`);

	if (!linha || !icon) return;

	linha.classList.toggle("d-none");

	const aberta = !linha.classList.contains("d-none");

	icon.classList.toggle("bi-chevron-right", !aberta);
	icon.classList.toggle("bi-chevron-down", aberta);
}