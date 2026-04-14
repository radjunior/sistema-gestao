document.getElementById("filtro-nome").addEventListener("input", function() {
	const filtro = this.value.toLowerCase();
	const linhas = document.querySelectorAll("#tb-cliente tbody tr");
	linhas.forEach(linha => {
		const nome = linha.children[1].textContent.toLowerCase();
		if (nome.includes(filtro)) {
			linha.style.display = "";
		} else {
			linha.style.display = "none";
		}
	});
});

window.confirmarExclusao = function(botao) {
	const nome = botao.getAttribute("data-nome");
	return confirm(`Deseja realmente excluir o cliente: ${nome}?`);
}
