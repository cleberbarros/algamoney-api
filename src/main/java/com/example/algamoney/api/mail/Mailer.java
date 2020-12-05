package com.example.algamoney.api.mail;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import com.example.algamoney.api.model.Lancamento;
import com.example.algamoney.api.repository.LancamentoRepository;

@Component
public class Mailer {

	@Autowired
	private JavaMailSender mailSender;
	
	@Autowired
	private TemplateEngine thymeleaf;
	
	@Autowired
	private LancamentoRepository repo;
	
//	@EventListener
//	private void teste(ApplicationReadyEvent event) {
//		this.enviarEmail("cleber.softagil@gmail.com", Arrays.asList("cleberbarros.ti@gmail.com"),
//				"Testndo", "Olá! <br/>Teste Ok");
//		System.out.println("Terminado o envio do e-mail");
//	}
	
	//APÊNDICE AULA: 22.19
//	@EventListener
//	private void teste(ApplicationReadyEvent event) {
//		String template = "mail/aviso-lancamentos-vencidos";
//		
//		List<Lancamento> lista = repo.findAll();
//		Map<String, Object> variaveis = new HashMap<>();
//		variaveis.put("lancamentos",lista);
//		
//		
//		
//		this.enviarEmail("cleber.softagil@gmail.com", Arrays.asList("cleberbarros.ti@gmail.com"),
//				"Testndo", template,variaveis);
//		System.out.println("Terminado o envio do e-mail");
//	}
//	
	
	//APÊNDICE AULA: 22.19
	public void enviarEmail(String remetente,
			List<String>destinatarios, String assunto,
			String template, Map<String,Object>variaveis) {
		
		Context context = new Context(new Locale("pt","BR"));
		
		variaveis.entrySet().forEach(
				e -> context.setVariable(e.getKey(), e.getValue()));
		
		String mensagem = thymeleaf.process(template, context);
		
		this.enviarEmail(remetente, destinatarios, assunto, mensagem);
	}
	
	public void enviarEmail(String remetente,
			List<String>destinatarios, String assunto, String mensagem) {
		
		try {
			MimeMessage mimeMessage = mailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "UTF-8");
			helper.setFrom(remetente);
			helper.setTo(destinatarios.toArray(new String[destinatarios.size()]));
			helper.setSubject(assunto);
			helper.setText(mensagem, true); //aqui o true é para sinalizar que o envio do e-mail sera em formato HTML
			
			mailSender.send(mimeMessage); //enviando o e-mail
			
		} catch (MessagingException e) {
			throw new RuntimeException("Problema com o envio do e-mail",e);
		}
	} 
}
