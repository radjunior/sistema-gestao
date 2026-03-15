const STORAGE_FORM_CADASTRO = "produto_form_cadastro_estado";
const STORAGE_FORM_VARIACAO = "produto_variacao_form_estado";

document.addEventListener("DOMContentLoaded", function() {
	const filtroNome = document.getElementById("filtro-produto");
	const filtroMarca = document.getElementById("filtro-marca");
	const filtroCategoria = document.getElementById("filtro-categoria");
	const filtroGrupo = document.getElementById("filtro-grupo");
	const filtroSubgrupo = document.getElementById("filtro-subgrupo");

	function aplicarFiltros() {
		const nome = (filtroNome?.value || "").trim().toLowerCase();
		const marca = filtroMarca?.value || "";
		const categoria = filtroCategoria?.value || "";
		const grupo = filtroGrupo?.value || "";
		const subgrupo = filtroSubgrupo?.value || "";

		const linhasProduto = document.querySelectorAll("#tb-produto tbody tr.linha-produto");

		linhasProduto.forEach(linha => {
			const id = linha.dataset.id;
			const nomeLinha = (linha.dataset.nome || "").toLowerCase();
			const marcaLinha = linha.dataset.marca || "";
			const categoriaLinha = linha.dataset.categoria || "";
			const grupoLinha = linha.dataset.grupo || "";
			const subgrupoLinha = linha.dataset.subgrupo || "";

			const atendeNome = !nome || nomeLinha.includes(nome);
			const atendeMarca = !marca || marcaLinha === marca;
			const atendeCategoria = !categoria || categoriaLinha === categoria;
			const atendeGrupo = !grupo || grupoLinha === grupo;
			const atendeSubgrupo = !subgrupo || subgrupoLinha === subgrupo;

			const exibir = atendeNome && atendeMarca && atendeCategoria && atendeGrupo && atendeSubgrupo;

			linha.style.display = exibir ? "" : "none";

			const linhaVariacoes = document.getElementById(`variacoes-${id}`);
			if (linhaVariacoes && !exibir) {
				linhaVariacoes.classList.add("d-none");

				const icon = document.getElementById(`icon-${id}`);
				if (icon) {
					icon.classList.remove("bi-chevron-down");
					icon.classList.add("bi-chevron-right");
				}
			}
		});
	}

	[filtroNome, filtroMarca, filtroCategoria, filtroGrupo, filtroSubgrupo]
		.filter(Boolean)
		.forEach(elemento => {
			elemento.addEventListener("input", aplicarFiltros);
			elemento.addEventListener("change", aplicarFiltros);
		});

	aplicarFiltros();
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