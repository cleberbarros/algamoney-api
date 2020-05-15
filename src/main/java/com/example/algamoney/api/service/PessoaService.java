package com.example.algamoney.api.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;

import com.example.algamoney.api.model.Pessoa;
import com.example.algamoney.api.repository.PessoaRepository;

@Service
public class PessoaService {
	
	@Autowired
	private PessoaRepository pessoaRepository;
	
	public Pessoa atualizar(Long codigo, Pessoa pessoa) {
		
		
		if(! pessoaRepository.existsById(codigo)) {
			throw new EmptyResultDataAccessException(1);  //lançando essa exceção que já esta tratada em AlgamoneyExceptionHandler
			//return ResponseEntity.notFound().build(); //esse outro foi como Thiago fez na oficia rest
		}
		
		/*preciso fazer pessoa.setId(codigo) senão o hibernate vai receber a pessoa vindo no json e no
		 * sem o codigo e vai entender que será um novo pessoa e vai adicionar ao inves de atualizar*/ 
		pessoa.setCodigo(codigo);
		pessoa = pessoaRepository.save(pessoa);
		
		return pessoa;
		
	}

	public void atualizarPropriedadeAtivo(Long codigo, Boolean ativo) {
		
		Pessoa pessoaSalva = buscarPessoaPeloCodigo(codigo);
		pessoaSalva.setAtivo(ativo);
		pessoaRepository.save(pessoaSalva);
		
	}

	public Pessoa buscarPessoaPeloCodigo(Long codigo) {
		Optional<Pessoa> pessoaSalva = pessoaRepository.findById(codigo);
		
		if(!pessoaSalva.isPresent()) {
			throw new EmptyResultDataAccessException(1);
		}
		
		return pessoaSalva.get();
	}
}
