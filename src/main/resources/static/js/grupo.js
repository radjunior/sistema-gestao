const TAB_SELECIONADA = "tab_selecionada";

document.addEventListener("DOMContentLoaded", function () {
	const inputNomeGrupo = document.getElementById("nome-grupo");
	if (inputNomeGrupo) {
		inputNomeGrupo.addEventListener("input", function () {
			const filtro = this.value.toLowerCase();
			const linhas = document.querySelectorAll("#tb-grupo tbody tr");
			linhas.forEach(linha => {
				const nome = linha.children[1]?.textContent.toLowerCase() || "";
				linha.style.display = nome.includes(filtro) ? "" : "none";
			});
		});
	}
	
	const inputNomeSubGrupo = document.getElementById("nome-subgrupo");
	if (inputNomeSubGrupo) {
		inputNomeSubGrupo.addEventListener("input", function () {
			const filtro = this.value.toLowerCase();
			const linhas = document.querySelectorAll("#tb-grupos tbody tr");
			linhas.forEach(linha => {
				const nome = linha.children[1]?.textContent.toLowerCase() || "";
				linha.style.display = nome.includes(filtro) ? "" : "none";
			});
		});
	}

	aplicarTab(getTab() || "grupo");
	document.documentElement.classList.remove("js-loading");
});

window.confirmarExclusao = function (botao) {
	const nome = botao.getAttribute("data-nome");
	return confirm(`Deseja realmente excluir: ${nome}?`);
};

window.mudarTab = function (element) {
	if (element.id === "a-grupo") {
		aplicarTab("grupo");
	}
	if (element.id === "a-subgrupo") {
		aplicarTab("subgrupo");
	}
};

function setTab(tab) {
	localStorage.setItem(TAB_SELECIONADA, tab);
}

function getTab() {
	return localStorage.getItem(TAB_SELECIONADA);
}

function aplicarTab(tab) {
	const isGrupo = tab === "grupo";

	setTab(tab);
	document.documentElement.dataset.tab = tab;

	document.getElementById("a-grupo")?.classList.toggle("active", isGrupo);
	document.getElementById("a-subgrupo")?.classList.toggle("active", !isGrupo);

	document.getElementById("card-cad-grupo")?.classList.toggle("js-hide", !isGrupo);
	document.getElementById("card-cad-subgrupo")?.classList.toggle("js-hide", isGrupo);

	document.getElementById("div-grupos")?.classList.toggle("js-hide", !isGrupo);
	document.getElementById("div-subgrupos")?.classList.toggle("js-hide", isGrupo);
}