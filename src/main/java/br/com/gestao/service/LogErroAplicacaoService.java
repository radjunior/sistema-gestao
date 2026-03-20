package br.com.gestao.service;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import br.com.gestao.entity.LogErroAplicacao;
import br.com.gestao.entity.Usuario;
import br.com.gestao.repository.LogErroAplicacaoRepository;
import jakarta.servlet.http.HttpServletRequest;

@Service
public class LogErroAplicacaoService {

	private final LogErroAplicacaoRepository logErroAplicacaoRepository;
	private final ContextoUsuarioService contextoUsuarioService;

	public LogErroAplicacaoService(LogErroAplicacaoRepository logErroAplicacaoRepository,
			ContextoUsuarioService contextoUsuarioService) {
		this.logErroAplicacaoRepository = logErroAplicacaoRepository;
		this.contextoUsuarioService = contextoUsuarioService;
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void registrarErro(Exception exception, HttpServletRequest request, String origem) {
		try {
			Usuario usuario = contextoUsuarioService.getUsuarioLogado();

			LogErroAplicacao log = new LogErroAplicacao();
			log.setDataHora(LocalDateTime.now());
			log.setNivel("ERROR");
			log.setOrigem(origem);
			log.setClasseExcecao(exception.getClass().getName());
			log.setMensagem(truncar(exception.getMessage(), 2000));
			log.setStackTrace(truncar(toStackTrace(exception), 12000));
			log.setUrl(request != null ? truncar(request.getRequestURI(), 500) : null);
			log.setMetodoHttp(request != null ? truncar(request.getMethod(), 10) : null);
			log.setLoginUsuario(usuario != null ? truncar(usuario.getUsuario(), 100) : null);
			log.setEmpresa(usuario != null ? usuario.getEmpresa() : null);
			log.setIp(request != null ? truncar(request.getRemoteAddr(), 50) : null);
			log.setUserAgent(request != null ? truncar(request.getHeader("User-Agent"), 1000) : null);

			logErroAplicacaoRepository.save(log);
		} catch (Exception ignored) {
			// O log de erro nao pode derrubar a resposta principal.
		}
	}

	@Transactional(readOnly = true)
	public List<LogErroAplicacao> consultar(String tipoFiltro, String termo) {
		if (termo == null || termo.isBlank()) {
			return logErroAplicacaoRepository.findTop200ByOrderByDataHoraDesc();
		}
		String valor = termo.trim();
		if ("classe".equalsIgnoreCase(tipoFiltro)) {
			return logErroAplicacaoRepository.findTop200ByClasseExcecaoContainingIgnoreCaseOrderByDataHoraDesc(valor);
		}
		return logErroAplicacaoRepository.findTop200ByMensagemContainingIgnoreCaseOrderByDataHoraDesc(valor);
	}

	private String toStackTrace(Exception exception) {
		StringWriter writer = new StringWriter();
		exception.printStackTrace(new PrintWriter(writer));
		return writer.toString();
	}

	private String truncar(String valor, int limite) {
		if (valor == null) {
			return null;
		}
		return valor.length() <= limite ? valor : valor.substring(0, limite);
	}
}
