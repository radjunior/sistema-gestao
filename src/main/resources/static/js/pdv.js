// Filtro de produto no select (busca por texto no select)
// Nenhum filtro extra necessario, o select ja mostra info util.

window.confirmarExclusao = function(botao) {
	const nome = botao.getAttribute("data-nome");
	return confirm(`Deseja realmente excluir: ${nome}?`);
}
