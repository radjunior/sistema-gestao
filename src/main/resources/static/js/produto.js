const STORAGE_FORM_CADASTRO = "produto_form_cadastro_estado";

document.addEventListener("DOMContentLoaded", function() {
	const filtroNome = document.getElementById("filtro-produto");
	const filtroMarca = document.getElementById("filtro-marca");
	const filtroGrupo = document.getElementById("filtro-grupo");
	const filtroSubgrupo = document.getElementById("filtro-subgrupo");
	const filtroTamanho = document.getElementById("filtro-tamanho");
	const filtroCodigoFabricante = document.getElementById("filtro-codigo-fabricante");

	const tabela = $("#tb-produto");

	if (tabela.length) {
		tabela.bootstrapTable();
	}

	function aplicarFiltros() {
		if (!tabela.length) return;

		const descricao = (filtroNome?.value || "").trim().toLowerCase();
		const marca = filtroMarca?.value || "";
		const grupo = filtroGrupo?.value || "";
		const subgrupo = filtroSubgrupo?.value || "";
		const tamanho = filtroTamanho?.value || "";
		const codigoFabricante = (filtroCodigoFabricante?.value || "").trim().toLowerCase();

		tabela.bootstrapTable("filterBy", {
			descricao,
			marcaId: marca,
			grupoId: grupo,
			subgrupoId: subgrupo,
			tamanhoId: tamanho,
			codigoFabricante
		}, {
			filterAlgorithm: function(row, filters) {
				const descricaoLinha = (row.descricao || "").toString().toLowerCase();
				const marcaLinha = (row.marcaId || "").toString();
				const grupoLinha = (row.grupoId || "").toString();
				const subgrupoLinha = (row.subgrupoId || "").toString();
				const tamanhoLinha = (row.tamanhoId || "").toString();
				const codigoFabricanteLinha = (row.codigoFabricante || "").toString().toLowerCase();

				const atendeDescricao = !filters.descricao || descricaoLinha.includes(filters.descricao);
				const atendeMarca = !filters.marcaId || marcaLinha === filters.marcaId;
				const atendeGrupo = !filters.grupoId || grupoLinha === filters.grupoId;
				const atendeSubgrupo = !filters.subgrupoId || subgrupoLinha === filters.subgrupoId;
				const atendeTamanho = !filters.tamanhoId || tamanhoLinha === filters.tamanhoId;
				const atendeCodigoFabricante = !filters.codigoFabricante || codigoFabricanteLinha.includes(filters.codigoFabricante);

				return atendeDescricao && atendeMarca && atendeGrupo && atendeSubgrupo && atendeTamanho && atendeCodigoFabricante;
			}
		});
	}

	[filtroNome, filtroMarca, filtroGrupo, filtroSubgrupo, filtroTamanho, filtroCodigoFabricante]
		.filter(Boolean)
		.forEach(elemento => {
			elemento.addEventListener("input", aplicarFiltros);
			elemento.addEventListener("change", aplicarFiltros);
		});

	aplicarFiltros();
	aplicarEstadoFormulario();

	const custoInput = document.getElementById("custo");
	const margemInput = document.getElementById("margem");
	const precoInput = document.getElementById("preco");

	if (custoInput) {
		custoInput.addEventListener("input", function() {
			aplicarMascaraMoeda(this);
			recalcularAPartirDoCampo("custo");
		});
	}

	if (margemInput) {
		margemInput.addEventListener("input", function() {
			recalcularAPartirDoCampo("margem");
		});
	}

	if (precoInput) {
		precoInput.addEventListener("input", function() {
			aplicarMascaraMoeda(this);
			recalcularAPartirDoCampo("preco");
		});
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

function recalcularAPartirDoCampo(origem) {
	const custoField = document.getElementById("custo");
	const margemField = document.getElementById("margem");
	const precoField = document.getElementById("preco");

	if (!custoField || !margemField || !precoField) return;

	const custo = parseMoeda(custoField.value);
	const margem = parseFloat((margemField.value || "0").replace(",", ".")) || 0;
	const preco = parseMoeda(precoField.value);

	if (origem === "margem" || origem === "custo") {
		const novoPreco = custo + (custo * margem / 100);
		precoField.value = formatarMoeda(novoPreco);
		return;
	}

	if (origem === "preco") {
		if (custo === 0) {
			margemField.value = "0.00";
			return;
		}
		const novaMargem = ((preco - custo) / custo) * 100;
		margemField.value = Number.isFinite(novaMargem) ? novaMargem.toFixed(2) : "0.00";
	}
}

function formatarMoeda(valor) {
	return Number(valor || 0).toLocaleString("pt-BR", {
		minimumFractionDigits: 2,
		maximumFractionDigits: 2
	});
}

function parseMoeda(valor) {
	if (!valor) return 0;
	return parseFloat(valor.toString().replace(/\./g, "").replace(",", ".")) || 0;
}

function aplicarMascaraMoeda(input) {
	let numeros = input.value.replace(/\D/g, "");
	if (numeros === "") numeros = "0";
	const valor = parseInt(numeros, 10) / 100;
	input.value = formatarMoeda(valor);
}
