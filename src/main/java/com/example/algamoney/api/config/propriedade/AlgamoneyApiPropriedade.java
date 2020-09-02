package com.example.algamoney.api.config.propriedade;

import org.springframework.boot.context.properties.ConfigurationProperties;

/*CLASSE DE CONFIGURAÇÃO PARA QUE SEJA POSSIVEL DEFINIR CONFIGURAÇÕES NO ARQUIVO CRIADO: application-prod.properties
 * QUE VAI SUBIR E SER EXECUTADO EM PRODUCAO, ISSO VAI SER DEFINIDO ATRAVÉS DO SUFIXO prod QUANDO FOR REALIZAR O DEPLOY DA APLICAÇÃO*/

@ConfigurationProperties("algamoney")
public class AlgamoneyApiPropriedade {

	private String originPermitida = "http://localhost:8000";
	
	private final Seguranca seguranca = new Seguranca();
	
	public Seguranca getSeguranca() {
		return seguranca;
	}

	

	public String getOriginPermitida() {
		return originPermitida;
	}



	public void setOriginPermitida(String originPermitida) {
		this.originPermitida = originPermitida;
	}



	public static class Seguranca{
		private boolean enableHttps;

		public boolean isEnableHttps() {
			return enableHttps;
		}

		public void setEnableHttps(boolean enableHttps) {
			this.enableHttps = enableHttps;
		}
		
	}
	
	
	
}
