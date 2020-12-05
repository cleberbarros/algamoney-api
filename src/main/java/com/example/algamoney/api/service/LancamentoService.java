package com.example.algamoney.api.service;

import java.io.InputStream;
import java.sql.Date;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import javax.validation.Valid;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.example.algamoney.api.dto.LancamentoEstatisticaPessoa;
import com.example.algamoney.api.model.Lancamento;
import com.example.algamoney.api.model.Pessoa;
import com.example.algamoney.api.repository.LancamentoRepository;
import com.example.algamoney.api.repository.PessoaRepository;
import com.example.algamoney.api.service.exception.PessoaInexistenteOuInativaException;

import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

@Service
public class LancamentoService {
	
	@Autowired
	private PessoaRepository pessoaRepository;
	
	@Autowired
	private LancamentoRepository lancamentoRepository;
	

	public Lancamento salvar(@Valid Lancamento lancamento) {

		Optional<Pessoa> pessoa = pessoaRepository.findById(lancamento.getPessoa().getCodigo());
		
		if(!pessoa.isPresent() || pessoa.get().isInativo() ) {
		//if(!pessoa.isPresent() || !pessoa.get().getAtivo() ) {
			throw new PessoaInexistenteOuInativaException();
		}
		return lancamentoRepository.save(lancamento) ;
	}
	
	public Lancamento atualizar(Long codigo, Lancamento lancamento) {
		
		Lancamento lancamentoSalvo = buscarLancamentoExistente(codigo);
		
		if( !lancamento.getPessoa().equals(lancamentoSalvo.getPessoa()) ) {
			validarPessoa(lancamento);
		}
		
		BeanUtils.copyProperties(lancamento, lancamentoSalvo, "codigo");
		
		return lancamentoRepository.save(lancamentoSalvo);
	}

	
	private void validarPessoa(Lancamento lancamento) {
		Optional<Pessoa> pessoa = null;
		if(lancamento.getPessoa().getCodigo() != null) {
			
			pessoa = pessoaRepository.findById(lancamento.getPessoa().getCodigo());
		}
		
		if( (!pessoa.isPresent()) || pessoa.get().isInativo() ) {
			throw new PessoaInexistenteOuInativaException();
		}
		
	

	}

	private Lancamento buscarLancamentoExistente(Long codigo) {

		Lancamento lancamentoPesquisado = lancamentoRepository.getOne(codigo);
		
		if(lancamentoPesquisado == null) {
			throw new IllegalArgumentException();
		}
		
				
		return lancamentoPesquisado;
	}
	
	
	// APÊNDICE AULA: 22.13
	public byte[] relatorioPorPessoa(LocalDate inicio, LocalDate fim) throws Exception{
		List<LancamentoEstatisticaPessoa> dados = lancamentoRepository.porPessoa(inicio, fim);
		
		//Criando um Map para passagem dos parametros para o relatório
		Map<String, Object> parametros = new HashMap<>();
		parametros.put("DT_INICIO", Date.valueOf(inicio));
		parametros.put("DT_FIM", Date.valueOf(fim));
		parametros.put("REPORT_LOCALE", new Locale("pt","BR"));
		
		InputStream inputStream = this.getClass()
				.getResourceAsStream("/relatorios/lancamentos-por-pessoa.jasper");
		
		JasperPrint jasperPrint = JasperFillManager.fillReport(inputStream, parametros, 
						new JRBeanCollectionDataSource(dados));
		
		return JasperExportManager.exportReportToPdf(jasperPrint);
		
		
	}
	
	// APÊNDICE AULA: 22.15
//	@Scheduled(fixedDelay = 1000 * 2)
//	public void avisarSobreLancamentosVencidos() {
//		System.out.println(">>>>>>>>>>>>Método sendo executado com FixedDelay...");
//	}
	
	// APÊNDICE AULA: 22.15
		//                 S M H Dia Mes DiaSemana                    
	    @Scheduled(cron = "0 0 6 * * *")
		public void avisarSobreLancamentosVencidos2() {
			System.out.println(">>>>>>>>>>>>Método sendo executado com Cron...");
		}
	

}
