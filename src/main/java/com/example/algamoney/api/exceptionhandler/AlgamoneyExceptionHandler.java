package com.example.algamoney.api.exceptionhandler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.example.algamoney.api.exceptionhandler.AlgamoneyExceptionHandler.Erro;
import com.example.algamoney.api.service.exception.PessoaInexistenteOuInativaException;

//captura execessões das entidades 

@ControllerAdvice  //essa classe esta com essa anotação para poder OBSERVAR TODA A APLICAÇÃO
public class AlgamoneyExceptionHandler extends ResponseEntityExceptionHandler{

	@Autowired
	private MessageSource messageSource; //acesso as mensagens do arquivo messages.properties
	
	//ESSE METODO TRATA MENSAGENS NÃO LIDAS.  OU SEJA OS ATRIBUTOS NÃO ENVIADOS. EX:
	//{
	// "nome": "teste",
	// "observacao": "nao quero"
	// }
	//na categoria não tenho o atributo observacao
	// EXISTE VARIAS OUTROS METODOS HANDLE QUE PODEMOS UTILIZAR
	@Override
	protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex,
			HttpHeaders headers, HttpStatus status, WebRequest request) {
		
		String mensagemUsuario = messageSource.getMessage("mensagem.invalida",null, LocaleContextHolder.getLocale());
		String mensagemDesenvolvedor = ex.getCause() != null ? ex.getCause().toString() : ex.toString();
		
		List<Erro>erros = Arrays.asList(new Erro(mensagemUsuario, mensagemDesenvolvedor));
		//Aqui no retorno passei no body uma mensagem tipo string e o status foi modificado para BAD_RESQUEST
		return handleExceptionInternal(ex, erros, headers, HttpStatus.BAD_REQUEST, request);
	}
	
	// ESSE METODO PEGA INFORMAÇÕES ENVIADAS DE FORMA INVALIDA, OU SEJA, QUE NÃO ESTA COERENTE
	// COM O BEAN VALIDATION NO MODEL DA CATEGORIA
	@Override
	protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
			HttpHeaders headers, HttpStatus status, WebRequest request) {

		List<Erro>erros = criarListaDeErros(ex.getBindingResult());  //em ex.getBindingResult() tem a lista de todos os erros eventualente ocorridos
		
		return handleExceptionInternal(ex,erros,headers, HttpStatus.BAD_REQUEST, request);
	}
	
	/*CRIANDO O METODO DE NOME: handleEmptyResultDataAccessException QUE VAI CAPTURA E TRATAR A EXCEÇÃO : 
	 * EmptyResultDataAccessException acontece QUANDO SE TENTA EXCLUIR INFORMAÇÃO NO BANCO QUE JÁ FOI EXCLUIDA  
	*/
	@ExceptionHandler({EmptyResultDataAccessException.class})
//	@ResponseStatus(HttpStatus.NOT_FOUND)
	public ResponseEntity<Object> handleEmptyResultDataAccessException(EmptyResultDataAccessException ex, WebRequest request) {
		
		String mensagemUsuario = messageSource.getMessage("recurso.nao-encontrado",null, LocaleContextHolder.getLocale());
		String mensagemDesenvolvedor = ex.toString();
		
		List<Erro>erros = Arrays.asList(new Erro(mensagemUsuario, mensagemDesenvolvedor));
		
		return handleExceptionInternal(ex, erros, new HttpHeaders(), HttpStatus.NOT_FOUND, request);
	}
	
	/*Método para tratar a Exceção DataIntegrityViolationException ou seja, quando não vai uma informação correto. 
	 * Esse errou ocorreu ao tentar inserir um lançamento com uma categoria que não existe o que deu erro na foreing key
	 * OBS: para pegar a mensagemDesenvolver vou necessário colocar a commons-lang3 no pom.xml*/
	@ExceptionHandler({DataIntegrityViolationException.class})
	public ResponseEntity<Object> handleDataIntegrityViolationException(DataIntegrityViolationException ex, WebRequest request){
		
		String mensagemUsuario = messageSource.getMessage("recurso.operacao-nao-permitida",null, LocaleContextHolder.getLocale());
		String mensagemDesenvolvedor = ExceptionUtils.getRootCauseMessage(ex);
		
		List<Erro>erros = Arrays.asList(new Erro(mensagemUsuario, mensagemDesenvolvedor));
		
		return handleExceptionInternal(ex, erros, new HttpHeaders(), HttpStatus.BAD_REQUEST, request);
	}
	
	
	/*TRATANDO A EXCESSÃO LANÇADA NO SALVAR DA CLASSE LancamentoService.
	 * */
	@ExceptionHandler({PessoaInexistenteOuInativaException.class} )
	public ResponseEntity<Object> handlePessoaInexistenteOuInativaException(PessoaInexistenteOuInativaException ex){
		
		String mensagemUsuario = messageSource.getMessage("pessoa.inexistente-ou-inativa",null, LocaleContextHolder.getLocale());
		String mensagemDesenvolvedor = ex.toString();
		Erro erro = new Erro(mensagemUsuario, mensagemDesenvolvedor);
		List<Erro>erros = Arrays.asList(new Erro(mensagemUsuario, mensagemDesenvolvedor));
		
		return ResponseEntity.badRequest().body(erros);
	}
	
	private List<Erro> criarListaDeErros(BindingResult bindingResult){
		List<Erro>erros = new ArrayList<>();
		
		for(FieldError fieldError: bindingResult.getFieldErrors() ) {  //getFieldErros() traz todos os campos(atributos) que tiveram erro provenientes do Bean Validation da Classe
			
			String mensagemUsuario = messageSource.getMessage(fieldError, LocaleContextHolder.getLocale());
			String mensagemDesenvolvedor = fieldError.toString();
			erros.add(new Erro(mensagemUsuario, mensagemDesenvolvedor));
		}
		
		return erros;
	}
	
	//CLASSE USADA SOMENTE AQUI POR ISSO CRIADO DENTRO
	public class Erro {
		private String mensagemUsuario;
		private String mensagemDesenvolvedor;
		
		public Erro(String mensagemUsuario, String mensagemDesenvolvedor) {
			this.mensagemUsuario = mensagemUsuario;
			this.mensagemDesenvolvedor = mensagemDesenvolvedor;
		}

		public String getMensagemUsuario() {
			return mensagemUsuario;
		}

		public String getMensagemDesenvolvedor() {
			return mensagemDesenvolvedor;
		}
		
	}
	
}
