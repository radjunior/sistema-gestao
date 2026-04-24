// Modal de quitacao de titulos a pagar

function formatBRL(v) {
	return 'R$ ' + Number(v).toLocaleString('pt-BR', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
}

$(function () {
	const modalEl = document.getElementById('modal-quitar');
	const modal = new bootstrap.Modal(modalEl);

	$('.btn-quitar').on('click', function () {
		const $b = $(this);
		const total = parseFloat($b.data('totalatualizado'));
		$('#mq-titulo-id').val($b.data('id'));
		$('#mq-info').text(`${$b.data('numero')}/${$b.data('total')}`);
		$('#mq-nominal').val(formatBRL($b.data('nominal')));
		$('#mq-juros').val(formatBRL($b.data('juros')));
		$('#mq-multa').val(formatBRL($b.data('multa')));
		$('#mq-total').val(formatBRL(total));
		$('#mq-valor-pago').val(total.toFixed(2));
		modal.show();
	});
});
