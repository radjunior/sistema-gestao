document.getElementById("nome").addEventListener("input", function () {
    const filtro = this.value.toLowerCase();
    const linhas = document.querySelectorAll("#tb-categoria tbody tr");
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
	console.log(nome);
	return confirm(`Deseja realmente excluir: ${nome}?`);
}