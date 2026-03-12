document.addEventListener('DOMContentLoaded', async function() {
	var list = [].slice.call(document.querySelectorAll('[data-bs-toggle="tooltip"]'))
	list.forEach((tooltip) => {
		new bootstrap.Tooltip(tooltip)
	})
});