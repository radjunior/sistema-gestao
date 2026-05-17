package br.com.gestao.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.gestao.entity.Caixa;
import br.com.gestao.entity.MovimentacaoCaixa;
import br.com.gestao.entity.Usuario;
import br.com.gestao.entity.Venda;
import br.com.gestao.entity.VendaPagamento;
import br.com.gestao.entity.enums.FormaPagamento;
import br.com.gestao.entity.enums.StatusCaixa;
import br.com.gestao.entity.enums.TipoMovimentacaoCaixa;
import br.com.gestao.repository.CaixaRepository;
import jakarta.persistence.EntityNotFoundException;

@Service
public class CaixaService {

	private final CaixaRepository caixaRepository;
	private final ContextoUsuarioService contextoUsuarioService;

	public CaixaService(CaixaRepository caixaRepository, ContextoUsuarioService contextoUsuarioService) {
		this.caixaRepository = caixaRepository;
		this.contextoUsuarioService = contextoUsuarioService;
	}

	public record ResumoCaixa(
			BigDecimal valorAbertura,
			BigDecimal totalVendas,
			BigDecimal totalSuprimentos,
			BigDecimal totalSangrias,
			BigDecimal dinheiroEsperado,
			Map<FormaPagamento, BigDecimal> totaisPorForma) {
	}

	public Caixa getCaixaAberto() {
		Usuario usuario = contextoUsuarioService.getUsuarioLogado();
		if (usuario == null) {
			return null;
		}
		return caixaRepository.findFirstByEmpresaIdAndUsuarioIdAndStatusOrderByDataAberturaDesc(
				contextoUsuarioService.getEmpresaIdObrigatoria(), usuario.getId(), StatusCaixa.ABERTO)
				.orElse(null);
	}

	public Caixa exigirCaixaAberto() throws Exception {
		Caixa caixa = getCaixaAberto();
		if (caixa == null) {
			throw new Exception("Nenhum caixa aberto. Abra o caixa antes de finalizar vendas.");
		}
		return caixa;
	}

	public List<Caixa> consultarHistorico() {
		return caixaRepository.findAllByEmpresaIdOrderByDataAberturaDesc(
				contextoUsuarioService.getEmpresaIdObrigatoria());
	}

	public Caixa consultarPorId(Long id) {
		return caixaRepository.findByIdAndEmpresaId(id, contextoUsuarioService.getEmpresaIdObrigatoria())
				.orElseThrow(() -> new EntityNotFoundException("Caixa nao encontrado!"));
	}

	@Transactional
	public Caixa abrirCaixa(BigDecimal valorAbertura, String observacao) throws Exception {
		if (getCaixaAberto() != null) {
			throw new Exception("Voce ja possui um caixa aberto. Feche-o antes de abrir outro.");
		}
		BigDecimal fundo = valorAbertura != null ? valorAbertura : BigDecimal.ZERO;
		if (fundo.signum() < 0) {
			throw new Exception("Valor de abertura invalido!");
		}

		Usuario usuario = contextoUsuarioService.getUsuarioLogado();
		LocalDateTime agora = LocalDateTime.now();

		Caixa caixa = new Caixa();
		caixa.setEmpresa(contextoUsuarioService.getEmpresaObrigatoria());
		caixa.setUsuario(usuario);
		caixa.setDataAbertura(agora);
		caixa.setValorAbertura(fundo);
		caixa.setStatus(StatusCaixa.ABERTO);
		caixa.setObservacaoAbertura(observacao);

		if (fundo.signum() > 0) {
			caixa.adicionarMovimentacao(novaMovimentacao(TipoMovimentacaoCaixa.ABERTURA, null, fundo,
					"Fundo de troco", null, agora));
		}

		return caixaRepository.save(caixa);
	}

	@Transactional
	public Caixa registrarSangria(BigDecimal valor, String descricao) throws Exception {
		Caixa caixa = exigirCaixaAberto();
		validarValorPositivo(valor);
		caixa.adicionarMovimentacao(novaMovimentacao(TipoMovimentacaoCaixa.SANGRIA, null, valor,
				descricao, null, LocalDateTime.now()));
		return caixaRepository.save(caixa);
	}

	@Transactional
	public Caixa registrarSuprimento(BigDecimal valor, String descricao) throws Exception {
		Caixa caixa = exigirCaixaAberto();
		validarValorPositivo(valor);
		caixa.adicionarMovimentacao(novaMovimentacao(TipoMovimentacaoCaixa.SUPRIMENTO, null, valor,
				descricao, null, LocalDateTime.now()));
		return caixaRepository.save(caixa);
	}

	/**
	 * Lanca no caixa aberto cada pagamento da venda finalizada.
	 * Pagamentos PARCELADO sao recebiveis (crediario), nao entram no caixa.
	 */
	@Transactional
	public void registrarVenda(Caixa caixa, Venda venda) {
		LocalDateTime agora = LocalDateTime.now();
		for (VendaPagamento pag : venda.getPagamentos()) {
			if (pag.getFormaPagamento() == FormaPagamento.PARCELADO) {
				continue;
			}
			caixa.adicionarMovimentacao(novaMovimentacao(TipoMovimentacaoCaixa.VENDA, pag.getFormaPagamento(),
					pag.getValor(), "Venda #" + venda.getId(), venda.getId(), agora));
		}
		caixaRepository.save(caixa);
	}

	@Transactional
	public Caixa fecharCaixa(BigDecimal valorInformado, String observacao) throws Exception {
		Caixa caixa = exigirCaixaAberto();
		if (valorInformado == null || valorInformado.signum() < 0) {
			throw new Exception("Informe o valor contado no caixa!");
		}

		BigDecimal esperado = calcularResumo(caixa).dinheiroEsperado();
		caixa.setValorFechamentoInformado(valorInformado);
		caixa.setValorFechamentoCalculado(esperado);
		caixa.setDiferenca(valorInformado.subtract(esperado));
		caixa.setObservacaoFechamento(observacao);
		caixa.setDataFechamento(LocalDateTime.now());
		caixa.setStatus(StatusCaixa.FECHADO);
		return caixaRepository.save(caixa);
	}

	public ResumoCaixa calcularResumo(Caixa caixa) {
		BigDecimal totalVendas = BigDecimal.ZERO;
		BigDecimal totalSuprimentos = BigDecimal.ZERO;
		BigDecimal totalSangrias = BigDecimal.ZERO;
		BigDecimal dinheiro = caixa.getValorAbertura();
		Map<FormaPagamento, BigDecimal> porForma = new EnumMap<>(FormaPagamento.class);

		for (MovimentacaoCaixa mov : caixa.getMovimentacoes()) {
			switch (mov.getTipo()) {
				case VENDA -> {
					totalVendas = totalVendas.add(mov.getValor());
					if (mov.getFormaPagamento() != null) {
						porForma.merge(mov.getFormaPagamento(), mov.getValor(), BigDecimal::add);
						if (mov.getFormaPagamento() == FormaPagamento.DINHEIRO) {
							dinheiro = dinheiro.add(mov.getValor());
						}
					}
				}
				case SUPRIMENTO -> {
					totalSuprimentos = totalSuprimentos.add(mov.getValor());
					dinheiro = dinheiro.add(mov.getValor());
				}
				case SANGRIA -> {
					totalSangrias = totalSangrias.add(mov.getValor());
					dinheiro = dinheiro.subtract(mov.getValor());
				}
				case ABERTURA -> {
					// ja contabilizado em valorAbertura
				}
			}
		}

		return new ResumoCaixa(caixa.getValorAbertura(), totalVendas, totalSuprimentos,
				totalSangrias, dinheiro, porForma);
	}

	private MovimentacaoCaixa novaMovimentacao(TipoMovimentacaoCaixa tipo, FormaPagamento forma,
			BigDecimal valor, String descricao, Long vendaId, LocalDateTime dataHora) {
		MovimentacaoCaixa mov = new MovimentacaoCaixa();
		mov.setTipo(tipo);
		mov.setFormaPagamento(forma);
		mov.setValor(valor);
		mov.setDescricao(descricao);
		mov.setVendaId(vendaId);
		mov.setDataHora(dataHora);
		return mov;
	}

	private void validarValorPositivo(BigDecimal valor) throws Exception {
		if (valor == null || valor.signum() <= 0) {
			throw new Exception("Valor invalido!");
		}
	}

}
