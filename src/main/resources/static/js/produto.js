const STORAGE_FORM_CADASTRO = "produto_form_cadastro_estado";

document.addEventListener("DOMContentLoaded", function() {
	const filtroNome = document.getElementById("filtro-produto");
	const filtroMarca = document.getElementById("filtro-marca");
	const filtroCategoria = document.getElementById("filtro-categoria");
	const filtroGrupo = document.getElementById("filtro-grupo");
	const filtroSubgrupo = document.getElementById("filtro-subgrupo");

	const tabela = $("#tb-produto");

	if (tabela.length) {
		tabela.bootstrapTable();
	}

	function aplicarFiltros() {
		if (!tabela.length) return;

		const nome = (filtroNome?.value || "").trim().toLowerCase();
		const marca = filtroMarca?.value || "";
		const categoria = filtroCategoria?.value || "";
		const grupo = filtroGrupo?.value || "";
		const subgrupo = filtroSubgrupo?.value || "";

		tabela.bootstrapTable("filterBy", {
			nome,
			marcaId: marca,
			categoriaId: categoria,
			grupoId: grupo,
			subgrupoId: subgrupo
		}, {
			filterAlgorithm: function(row, filters) {
				const nomeLinha = (row.nome || "").toString().toLowerCase();
				const marcaLinha = (row.marcaId || "").toString();
				const categoriaLinha = (row.categoriaId || "").toString();
				const grupoLinha = (row.grupoId || "").toString();
				const subgrupoLinha = (row.subgrupoId || "").toString();

				const atendeNome = !filters.nome || nomeLinha.includes(filters.nome);
				const atendeMarca = !filters.marcaId || marcaLinha === filters.marcaId;
				const atendeCategoria = !filters.categoriaId || categoriaLinha === filters.categoriaId;
				const atendeGrupo = !filters.grupoId || grupoLinha === filters.grupoId;
				const atendeSubgrupo = !filters.subgrupoId || subgrupoLinha === filters.subgrupoId;

				return atendeNome && atendeMarca && atendeCategoria && atendeGrupo && atendeSubgrupo;
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

	const custoInput = document.getElementById("custo");
	const margemInput = document.getElementById("margem");

	if (custoInput) {
		custoInput.addEventListener("input", function() {
			aplicarMascaraMoeda(this);
		});
	}

	if (margemInput) {
		margemInput.addEventListener("input", calcularPreco);
	}
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

	if (!form) return;

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

	if (!btnToggle || !form) return;

	const recolhido = form.classList.contains("recolhido");
	btnToggle.classList.toggle("bi-arrow-bar-up", !recolhido);
	btnToggle.classList.toggle("bi-arrow-bar-down", recolhido);
}

window.confirmarExclusao = function(botao) {
	const nome = botao.getAttribute("data-nome");
	return confirm(`Deseja realmente excluir: ${nome}?`);
};

function calcularPreco() {
	const custoField = document.getElementById("custo");
	const margemField = document.getElementById("margem");
	const precoField = document.getElementById("preco");

	if (!custoField || !margemField || !precoField) return;

	const custo = parseMoeda(custoField.value);
	const margem = parseFloat(margemField.value) || 0;
	const preco = custo + (custo * margem / 100);
	precoField.value = formatarMoeda(preco);
}

function formatarMoeda(valor) {
	return valor.toLocaleString("pt-BR", {
		minimumFractionDigits: 2,
		maximumFractionDigits: 2
	});
}

function parseMoeda(valor) {
	if (!valor) return 0;
	return parseFloat(valor.replace(/\./g, "").replace(",", "."));
}

function aplicarMascaraMoeda(input) {
	let numeros = input.value.replace(/\D/g, "");
	if (numeros === "") numeros = "0";
	const valor = parseInt(numeros, 10) / 100;
	input.value = formatarMoeda(valor);
	calcularPreco();
}