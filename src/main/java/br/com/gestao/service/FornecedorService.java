package br.com.gestao.service;

import java.util.List;

import org.springframework.stereotype.Service;

import br.com.gestao.entity.Fornecedor;
import br.com.gestao.repository.FornecedorRepository;
import jakarta.persistence.EntityNotFoundException;

@Service
public class FornecedorService {

	private final FornecedorRepository fornecedorRepository;
	private final ContextoUsuarioService contextoUsuarioService;

	public FornecedorService(FornecedorRepository fornecedorRepository,
			ContextoUsuarioService contextoUsuarioService) {
		this.fornecedorRepository = fornecedorRepository;
		this.contextoUsuarioService = contextoUsuarioService;
	}

	public List<Fornecedor> consultar() {
		return fornecedorRepository
				.findAllByEmpresaIdOrderByNomeAsc(contextoUsuarioService.getEmpresaIdObrigatoria());
	}

	public Fornecedor consultarPorId(Long id) {
		return fornecedorRepository
				.findByIdAndEmpresaId(id, contextoUsuarioService.getEmpresaIdObrigatoria())
				.orElseThrow(() -> new EntityNotFoundException("Fornecedor nao encontrado!"));
	}

	public void salvar(Fornecedor fornecedor) throws Exception {
		if (fornecedor == null || fornecedor.getNome() == null || fornecedor.getNome().isBlank()) {
			throw new Exception("Nome invalido!");
		}
		Long empresaId = contextoUsuarioService.getEmpresaIdObrigatoria();
		String nome = fornecedor.getNome().trim();
		String cnpj = fornecedor.getCnpj() != null ? fornecedor.getCnpj().replaceAll("[^0-9]", "") : null;
		if (cnpj != null && cnpj.isBlank()) {
			cnpj = null;
		}

		if (cnpj != null) {
			if (fornecedor.getId() == null) {
				if (fornecedorRepository.existsByCnpjAndEmpresaId(cnpj, empresaId)) {
					throw new Exception("CNPJ ja cadastrado!");
				}
			} else {
				if (fornecedorRepository.existsByCnpjAndEmpresaIdAndIdNot(cnpj, empresaId, fornecedor.getId())) {
					throw new Exception("CNPJ ja cadastrado!");
				}
			}
		}

		if (fornecedor.getId() == null) {
			fornecedor.setNome(nome);
			fornecedor.setCnpj(cnpj);
			fornecedor.setEmpresa(contextoUsuarioService.getEmpresaObrigatoria());
			if (fornecedor.getAtivo() == null) {
				fornecedor.setAtivo(true);
			}
			fornecedorRepository.save(fornecedor);
			return;
		}

		Fornecedor salvo = consultarPorId(fornecedor.getId());
		salvo.setNome(nome);
		salvo.setNomeFantasia(fornecedor.getNomeFantasia());
		salvo.setCnpj(cnpj);
		salvo.setTelefone(fornecedor.getTelefone());
		salvo.setEmail(fornecedor.getEmail());
		salvo.setEndereco(fornecedor.getEndereco());
		salvo.setAtivo(Boolean.TRUE.equals(fornecedor.getAtivo()));
		fornecedorRepository.save(salvo);
	}

	public void excluir(Fornecedor fornecedor) {
		fornecedorRepository.delete(consultarPorId(fornecedor.getId()));
	}

}
