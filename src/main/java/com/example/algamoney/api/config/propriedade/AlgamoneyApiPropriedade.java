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
	
	//APÊNDICE AUla: 22.30
	private final S3 s3 = new S3();
		
	public S3 getS3() {
		return s3;
	}

	//APÊNDICE AULA: 22.16
	private final Mail mail = new Mail();
	public Mail getMail() {
		return mail;
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
	
	//APÊNDICE AULA: 22.16
	public static class Mail{
		private String host;
		private Integer port;
		private String username;
		private String password;
		public String getHost() {
			return host;
		}
		public void setHost(String host) {
			this.host = host;
		}
		public Integer getPort() {
			return port;
		}
		public void setPort(Integer port) {
			this.port = port;
		}
		public String getUsername() {
			return username;
		}
		public void setUsername(String username) {
			this.username = username;
		}
		public String getPassword() {
			return password;
		}
		public void setPassword(String password) {
			this.password = password;
		}
		
		
		
	}
	
//	Aula 22.30
	
	public static class S3{
		private String accessKeyId;
		private String secretAccessKey;
		
		private String bucket = "cl-algamoney-arquivos";
		
		public String getBucket() {
			return bucket;
		}
		
		public void setBucket(String bucket) {
			this.bucket = bucket;
		}
		
		public String getAccessKeyId() {
			return accessKeyId;
		}
		public void setAccessKeyId(String accessKeyId) {
			this.accessKeyId = accessKeyId;
		}
		public String getSecretAccessKey() {
			return secretAccessKey;
		}
		public void setSecretAccessKey(String secretAccessKey) {
			this.secretAccessKey = secretAccessKey;
		}
		
	}
	
	
	
}
