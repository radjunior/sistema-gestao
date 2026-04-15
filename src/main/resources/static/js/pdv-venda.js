// PDV - logica de venda com suporte a parcelamento

const itens = [];

function formatBRL(v) {
	return 'R$ ' + Number(v).toLocaleString('pt-BR', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
}

function parseNum(v) {
	if (v === null || v === undefined || v === '') return 0;
	return parseFloat(v) || 0;
}

function subtotal() {
	return itens.reduce((s, i) => s + (i.quantidade * i.precoUnitario), 0);
}

function totalComDesconto() {
	const d = parseNum($('#inp-desconto').val());
	return Math.max(0, subtotal() - d);
}

function renderItens() {
	const $tbody = $('#tb-itens tbody');
	$tbody.empty();
	if (itens.length === 0) {
		$tbody.append('<tr><td colspan="5" class="text-center text-muted">Nenhum item adicionado.</td></tr>');
	}
	itens.forEach((item, idx) => {
		const sub = item.quantidade * item.precoUnitario;
		$tbody.append(`
			<tr>
				<td>${item.descricao}</td>
				<td>${item.quantidade}</td>
				<td>${formatBRL(item.precoUnitario)}</td>
				<td>${formatBRL(sub)}</td>
				<td><button type="button" class="btn btn-sm btn-outline-danger btn-rm-item" data-idx="${idx}"><i class="bi bi-x"></i></button></td>
			</tr>
		`);
	});
	$('#tfoot-subtotal').text(formatBRL(subtotal()));
	atualizarTotais();
}

function atualizarTotais() {
	const total = totalComDesconto();
	$('#lbl-total').text(formatBRL(total));
	atualizarPreviewParcelas();
}

function calcularParcelaPrice(pv, n, taxaPerc) {
	if (!taxaPerc || taxaPerc <= 0) return pv / n;
	const i = taxaPerc / 100;
	return pv * (i / (1 - Math.pow(1 + i, -n)));
}

function atualizarPreviewParcelas() {
	const forma = $('#sel-forma').val();
	const isParcelado = forma === 'PARCELADO';
	$('#box-parcelado').toggleClass('d-none', !isParcelado);
	if (!isParcelado) {
		$('#box-total-juros').addClass('d-none');
		return;
	}

	const total = totalComDesconto();
	const n = parseInt($('#inp-parcelas').val()) || 1;
	const comJuros = $('#chk-com-juros').is(':checked');
	const taxa = parseNum($('#inp-taxa').val());
	const diasPrimeira = parseInt($('#inp-dias-primeira').val()) || 30;

	$('#box-taxa').toggleClass('d-none', !comJuros);

	if (total <= 0 || n < 1) {
		$('#preview-parcelas').html('<small>Adicione itens para visualizar as parcelas.</small>');
		$('#box-total-juros').addClass('d-none');
		return;
	}

	let valorParcela;
	let totalJuros;
	if (comJuros) {
		valorParcela = calcularParcelaPrice(total, n, taxa);
		totalJuros = valorParcela * n;
	} else {
		valorParcela = total / n;
		totalJuros = total;
	}

	const primeira = new Date();
	primeira.setDate(primeira.getDate() + diasPrimeira);

	let html = `<strong>${n}x de ${formatBRL(valorParcela)}</strong><br>`;
	html += `<small>Total: ${formatBRL(totalJuros)}`;
	if (comJuros) html += ` <span class="text-warning">(juros: ${formatBRL(totalJuros - total)})</span>`;
	html += `<br>1a parcela: ${primeira.toLocaleDateString('pt-BR')}</small>`;
	$('#preview-parcelas').html(html);

	if (comJuros) {
		$('#box-total-juros').removeClass('d-none');
		$('#lbl-total-juros').text(formatBRL(totalJuros));
	} else {
		$('#box-total-juros').addClass('d-none');
	}
}

$(function () {
	$('#sel-produto').on('change', function () {
		const preco = $(this).find(':selected').data('preco');
		if (preco !== undefined) $('#inp-preco').val(preco);
	});

	$('#btn-add-item').on('click', function () {
		const $opt = $('#sel-produto').find(':selected');
		const produtoId = $('#sel-produto').val();
		if (!produtoId) return alert('Selecione um produto.');
		const qtd = parseNum($('#inp-qtd').val());
		const preco = parseNum($('#inp-preco').val());
		if (qtd <= 0) return alert('Quantidade invalida.');
		if (preco < 0) return alert('Preco invalido.');
		itens.push({
			produtoId: parseInt(produtoId),
			descricao: $opt.data('descricao'),
			quantidade: qtd,
			precoUnitario: preco
		});
		$('#sel-produto').val('');
		$('#inp-qtd').val(1);
		$('#inp-preco').val(0);
		renderItens();
	});

	$('#tb-itens').on('click', '.btn-rm-item', function () {
		const idx = parseInt($(this).data('idx'));
		itens.splice(idx, 1);
		renderItens();
	});

	$('#inp-desconto, #sel-forma, #inp-parcelas, #chk-com-juros, #inp-taxa, #inp-dias-primeira').on('input change', atualizarTotais);

	$('#btn-finalizar').on('click', async function () {
		if (itens.length === 0) return alert('Adicione ao menos um item.');
		const forma = $('#sel-forma').val();
		const clienteId = $('#sel-cliente').val();
		if (forma === 'PARCELADO' && !clienteId) {
			return alert('Selecione um cliente para venda parcelada.');
		}

		const payload = {
			clienteId: clienteId ? parseInt(clienteId) : null,
			formaPagamento: forma,
			totalParcelas: forma === 'PARCELADO' ? (parseInt($('#inp-parcelas').val()) || 1) : 1,
			comJuros: $('#chk-com-juros').is(':checked'),
			taxaJurosMensal: parseNum($('#inp-taxa').val()),
			diasPrimeiraParcela: parseInt($('#inp-dias-primeira').val()) || 30,
			valorDesconto: parseNum($('#inp-desconto').val()),
			observacao: $('#inp-observacao').val(),
			itens: itens.map(i => ({
				produtoId: i.produtoId,
				quantidade: i.quantidade,
				precoUnitario: i.precoUnitario
			}))
		};

		const $btn = $(this);
		$btn.prop('disabled', true);
		try {
			const csrfToken = $('meta[name="_csrf"]').attr('content');
			const csrfHeader = $('meta[name="_csrf_header"]').attr('content');
			const headers = { 'Content-Type': 'application/json' };
			if (csrfToken && csrfHeader) headers[csrfHeader] = csrfToken;

			const resp = await fetch('/pdv/venda/finalizar', {
				method: 'POST',
				headers,
				body: JSON.stringify(payload)
			});
			const data = await resp.json();
			if (!resp.ok || !data.ok) {
				alert('Erro: ' + (data.mensagem || 'Falha ao finalizar venda.'));
				$btn.prop('disabled', false);
				return;
			}
			alert(data.mensagem);
			window.location.reload();
		} catch (e) {
			alert('Erro de comunicacao: ' + e.message);
			$btn.prop('disabled', false);
		}
	});

	renderItens();
});
