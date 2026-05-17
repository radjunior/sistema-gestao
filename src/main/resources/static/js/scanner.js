// Helper reutilizavel de leitura de codigo de barras.
//
// Leitores de codigo de barras agem como teclado: digitam os caracteres
// muito rapido e finalizam com Enter. Esta funcao detecta esse padrao
// (sequencia rapida + Enter, ou simplesmente Enter manual) e tenta
// resolver o produto pelo codigo de barras exato. Se encontrar, dispara
// o callback e impede o comportamento padrao do Enter.

const LIMITE_MS_ENTRE_TECLAS = 35; // teclas mais rapidas que isso = leitor

export async function resolverCodigoBarra(codigo) {
	const cb = (codigo || "").trim();
	if (!cb) return null;
	try {
		const resp = await fetch(`/api/produto/por-codigo-barra?codigo=${encodeURIComponent(cb)}`);
		if (!resp.ok) return null;
		return await resp.json();
	} catch (e) {
		console.error("Erro ao resolver codigo de barras:", e);
		return null;
	}
}

/**
 * Liga deteccao de bipagem a um input.
 * @param {HTMLInputElement} input
 * @param {(produto:object)=>void} onProduto chamado quando o codigo bipado
 *        casa exatamente com um produto.
 * @param {{minLength?:number}} [opts]
 */
export function attachBarcodeScanner(input, onProduto, opts = {}) {
	if (!input) return;
	const minLength = opts.minLength ?? 3;
	let ultimaTecla = 0;
	let pareceLeitor = false;

	input.addEventListener("keydown", async (ev) => {
		const agora = Date.now();
		if (ev.key === "Enter") {
			const valor = input.value.trim();
			if (valor.length < minLength) {
				pareceLeitor = false;
				return;
			}
			const produto = await resolverCodigoBarra(valor);
			if (produto) {
				ev.preventDefault();
				ev.stopPropagation();
				onProduto(produto);
			}
			pareceLeitor = false;
			return;
		}
		if (ev.key.length === 1) {
			pareceLeitor = ultimaTecla !== 0 && (agora - ultimaTecla) <= LIMITE_MS_ENTRE_TECLAS;
			ultimaTecla = agora;
		}
	});
}
