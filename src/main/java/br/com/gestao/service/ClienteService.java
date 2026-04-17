package br.com.gestao.service;

import java.util.List;

import org.springframework.stereotype.Service;

import br.com.gestao.entity.Cliente;
import br.com.gestao.repository.ClienteRepository;
import jakarta.persistence.EntityNotFoundException;

@Service
public class ClienteService {

	private final ClienteRepository clienteRepository;
	private final ContextoUsuarioService contextoUsuarioService;

	public ClienteService(ClienteRepository clienteRepository, ContextoUsuarioService contextoUsuarioService) {
		this.clienteRepository = clienteRepository;
		this.contextoUsuarioService = contextoUsuarioService;
	}

	public List<Cliente> consultar() {
		return clienteRepository.findAllByEmpresaIdOrderByNomeAsc(contextoUsuarioService.getEmpresaIdObrigatoria());
	}

	public Cliente consultarPorId(Long id) {
		return clienteRepository.findByIdAndEmpresaId(id, contextoUsuarioService.getEmpresaIdObrigatoria())
				.orElseThrow(() -> new EntityNotFoundException("Cliente nao encontrado!"));
	}

	public void salvar(Cliente cliente) throws Exception {
		if (cliente == null || cliente.getNome() == null || cliente.getNome().isBlank()) {
			throw new Exception("Nome invalido!");
		}

		Long empresaId = contextoUsuarioService.getEmpresaIdObrigatoria();
		String nome = cliente.getNome().trim();
		String cpf = cliente.getCpf() != null ? cliente.getCpf().replaceAll("[^0-9]", "") : null;

		if (cpf != null && !cpf.isBlank()) {
			if (cliente.getId() == null) {
				if (clienteRepository.existsByCpfAndEmpresaId(cpf, empresaId)) {
					throw new Exception("CPF ja cadastrado!");
				}
			} else {
				if (clienteRepository.existsByCpfAndEmpresaIdAndIdNot(cpf, empresaId, cliente.getId())) {
					throw new Exception("CPF ja cadastrado!");
				}
			}
		}

		if (cliente.getId() == null) {
			cliente.setNome(nome);
			cliente.setCpf(cpf);
			cliente.setEmpresa(contextoUsuarioService.getEmpresaObrigatoria());
			if (cliente.getAtivo() == null) {
				cliente.setAtivo(true);
			}
			clienteRepository.save(cliente);
			return;
		}

		Cliente clienteSalvo = consultarPorId(cliente.getId());
		clienteSalvo.setNome(nome);
		clienteSalvo.setCpf(cpf);
		clienteSalvo.setTelefone(cliente.getTelefone());
		clienteSalvo.setEmail(cliente.getEmail());
		clienteSalvo.setEndereco(cliente.getEndereco());
		clienteSalvo.setObservacao(cliente.getObservacao());
		clienteSalvo.setAtivo(Boolean.TRUE.equals(cliente.getAtivo()));
		clienteRepository.save(clienteSalvo);
	}

	public void excluir(Cliente cliente) {
		clienteRepository.delete(consultarPorId(cliente.getId()));
	}

}
