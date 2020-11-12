package com.example.algamoney.api.resource;


import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.MessageSource;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.example.algamoney.api.dto.LancamentoEstatisticaCategoria;
import com.example.algamoney.api.dto.LancamentoEstatisticaDia;
import com.example.algamoney.api.event.RecursoCriadoEvent;
import com.example.algamoney.api.model.Lancamento;
import com.example.algamoney.api.repository.LancamentoRepository;
import com.example.algamoney.api.repository.filter.LancamentoFilter;
import com.example.algamoney.api.repository.projecao.ResumoLancamento;
import com.example.algamoney.api.service.LancamentoService;

@RestController
@RequestMapping("/lancamentos")
public class LancamentoResource {

	@Autowired
	private LancamentoRepository lancamentoRepository;
	
	@Autowired
	private LancamentoService lancamentoService;
	
	
	@Autowired
	private ApplicationEventPublisher publisher;
	
	@Autowired
	private MessageSource messageSource; 
	
	// Pageable para fazer a paginação
	@GetMapping
	@PreAuthorize("hasAuthority('ROLE_PESQUISAR_LANCAMENTO')and #oauth2.hasScope('read')")
	public Page<Lancamento> pesquisar(LancamentoFilter lancamentoFilter, Pageable pageable){
		return lancamentoRepository.filtrar(lancamentoFilter, pageable);
	}
	
	//Fazendo uso de projeções
	@GetMapping (params = "resumo") //http://localhost:8080/lancamentos?resumo
	@PreAuthorize("hasAuthority('ROLE_PESQUISAR_LANCAMENTO')and #oauth2.hasScope('read')")
	public Page<ResumoLancamento> resumir(LancamentoFilter lancamentoFilter, Pageable pageable){
		return lancamentoRepository.resumir(lancamentoFilter, pageable);
	}
	
	@GetMapping("/{codigo}")
	@PreAuthorize("hasAuthority('ROLE_PESQUISAR_LANCAMENTO')and #oauth2.hasScope('read')")
	public ResponseEntity<Lancamento> buscar(@PathVariable Long codigo) {
		
	Optional<Lancamento> lancamentoPesquisado = lancamentoRepository.findById(codigo);
	
	
	if(!lancamentoPesquisado.isPresent()) {
		throw new EmptyResultDataAccessException(1);
	}
	
	return ResponseEntity.ok(lancamentoPesquisado.get());
	
		
	}
	
	@PostMapping
	@PreAuthorize("hasAuthority('ROLE_CADASTRAR_LANCAMENTO')and #oauth2.hasScope('write')")
	public ResponseEntity<Lancamento> criar (@Valid @RequestBody Lancamento lancamento, HttpServletResponse response){
	  Lancamento lancamentoSalvo =lancamentoService.salvar(lancamento);
	  
	  publisher.publishEvent(new RecursoCriadoEvent(this, response, lancamentoSalvo.getCodigo()));  
	  
	  return ResponseEntity.ok(lancamentoSalvo); 
	}
	
	
	@DeleteMapping("/{codigo}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@PreAuthorize("hasAuthority('ROLE_REMOVER_LANCAMENTO')and #oauth2.hasScope('write')")
	public void excluir(@PathVariable Long codigo ){
		
		lancamentoRepository.deleteById(codigo);
	}

	@PutMapping("/{codigo}")
	@PreAuthorize("hasAuthority('ROLE_CADASTRAR_LANCAMENTO')")
	public ResponseEntity<Lancamento> atualizar (@PathVariable Long codigo, @Valid @RequestBody Lancamento lancamento){
		
		try {
			Lancamento lancamentoSalvo = lancamentoService.atualizar(codigo, lancamento);
			return ResponseEntity.ok(lancamentoSalvo);
		}catch(IllegalArgumentException e ) {
			return ResponseEntity.notFound().build();
		}
	}

	//CODIGO AULA APÊNDICE: 22.3
	@GetMapping("/estatisticas/por-categoria")
	@PreAuthorize("hasAuthority('ROLE_PESQUISAR_LANCAMENTO')and #oauth2.hasScope('read')")
	public List<LancamentoEstatisticaCategoria> porCategoria(){
		
		return this.lancamentoRepository.porCategoria(LocalDate.now());
	}
	
	
	//CODIGO AULA APÊNDICE: 22.5
	@GetMapping("/estatisticas/por-dia")
	@PreAuthorize("hasAuthority('ROLE_PESQUISAR_LANCAMENTO')and #oauth2.hasScope('read')")
	public List<LancamentoEstatisticaDia> porDia(){
		
		return this.lancamentoRepository.porDia(LocalDate.now());
	}
	
	//CODIGO AULA APÊNCIDE: 22.14
	@GetMapping("/relatorios/por-pessoa")
	@PreAuthorize("hasAuthority('ROLE_PESQUISAR_LANCAMENTO')and #oauth2.hasScope('read')")
	public ResponseEntity<byte[]> relatorioPorPessoa(
			@RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate inicio,
			@RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate fim) throws Exception{
		byte[] relatorio = lancamentoService.relatorioPorPessoa(inicio, fim);
		
		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_PDF_VALUE)
				.body(relatorio);
		
	}
	

}
