document.getElementById("filtro-cliente").addEventListener("input", function() {
	const filtro = this.value.toLowerCase();
	const linhas = document.querySelectorAll("#tb-vendas tbody tr");
	linhas.forEach(linha => {
		const cliente = linha.children[2].textContent.toLowerCase();
		if (cliente.includes(filtro)) {
			linha.style.display = "";
		} else {
			linha.style.display = "none";
		}
	});
});
