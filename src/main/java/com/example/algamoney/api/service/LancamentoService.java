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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.example.algamoney.api.dto.LancamentoEstatisticaPessoa;
import com.example.algamoney.api.mail.Mailer;
import com.example.algamoney.api.model.Lancamento;
import com.example.algamoney.api.model.Pessoa;
import com.example.algamoney.api.model.Usuario;
import com.example.algamoney.api.repository.LancamentoRepository;
import com.example.algamoney.api.repository.PessoaRepository;
import com.example.algamoney.api.repository.UsuarioRepository;
import com.example.algamoney.api.service.exception.PessoaInexistenteOuInativaException;
import com.example.algamoney.api.storage.S3;

import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

@Service
public class LancamentoService {
	
	private static final String DESTINATARIOS = "ROLE_PESQUISAR_LANCAMENTO";
	
	//APENDICE Aula 22.22
	private static final Logger logger = LoggerFactory.getLogger(LancamentoService.class);
	
	@Autowired
	private PessoaRepository pessoaRepository;
	
	@Autowired
	private LancamentoRepository lancamentoRepository;
	
	@Autowired
	private UsuarioRepository usuarioRepository;
	
	@Autowired
	private Mailer mailer;
	
	@Autowired
	private S3 s3;

	public Lancamento salvar(@Valid Lancamento lancamento) {

		Optional<Pessoa> pessoa = pessoaRepository.findById(lancamento.getPessoa().getCodigo());
		
		if(!pessoa.isPresent() || pessoa.get().isInativo() ) {
		//if(!pessoa.isPresent() || !pessoa.get().getAtivo() ) {
			throw new PessoaInexistenteOuInativaException();
		}
		
		//APENDICE AULA 22.34
		if(StringUtils.hasText(lancamento.getAnexo())) {  //SE TIVER TEXTO NO ANEXO
			s3.salvar(lancamento.getAnexo());
		}
		
		return lancamentoRepository.save(lancamento) ;
	}
	
	public Lancamento atualizar(Long codigo, Lancamento lancamento) {
		
		Lancamento lancamentoSalvo = buscarLancamentoExistente(codigo);
		
		if( !lancamento.getPessoa().equals(lancamentoSalvo.getPessoa()) ) {
			validarPessoa(lancamento);
		}
		
		if (StringUtils.isEmpty(lancamento.getAnexo())
				&& StringUtils.hasText(lancamentoSalvo.getAnexo())){
			s3.remover(lancamentoSalvo.getAnexo());
		}else if (StringUtils.hasLength(lancamento.getAnexo())
				&& !lancamento.getAnexo().equals(lancamentoSalvo.getAnexo())) {
			s3.substituir(lancamentoSalvo.getAnexo(), lancamento.getAnexo());
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
		//@Scheduled(fixedDelay = 1000 * 60 * 30) //equivalente a 30 minutos
		public void avisarSobreLancamentosVencidos() {
	    	
	    	//APENDICE Aula 22.22
	    	if(logger.isDebugEnabled()) {
	    		logger.debug("Preparando envio e e-mails e aviso e lançamentos vencidos.");
	    	}
	    	
			
			List<Lancamento> vencidos = lancamentoRepository.
					findByDataVencimentoLessThanEqualAndDataPagamentoIsNull(LocalDate.now());
			
			//APENDICE Aula 22.22
			if(vencidos.isEmpty()) {
				logger.info("Sem lançamento vencidos para aviso.");
				return;
			}
			//APENDICE Aula 22.22
			logger.info("Existem {} lançamentos vencidos.",vencidos.size());
			
			
			List<Usuario> destinatarios = usuarioRepository.
					findByPermissoesDescricao(DESTINATARIOS);
			
			//APENDICE Aula 22.22
			if(destinatarios.isEmpty()) {
				logger.warn("Existem lançamentos vencidos, mas o sistema não encontrou destinatarios validos");
				return;
			}
			
			
			//Aula 22.21
			mailer.avisarSobreLancamentosVencidos(vencidos, destinatarios);
			
			
			//APENDICE Aula 22.22
			logger.info("Envio de e-mail de aviso concluido!");
		}
	

}
