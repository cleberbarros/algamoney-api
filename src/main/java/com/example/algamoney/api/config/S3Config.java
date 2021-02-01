package com.example.algamoney.api.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.BucketLifecycleConfiguration;
import com.amazonaws.services.s3.model.CreateBucketRequest;
import com.amazonaws.services.s3.model.Tag;
import com.amazonaws.services.s3.model.lifecycle.LifecycleFilter;
import com.amazonaws.services.s3.model.lifecycle.LifecycleTagPredicate;
import com.example.algamoney.api.config.propriedade.AlgamoneyApiPropriedade;

@Configuration
public class S3Config {

	@Autowired
	private AlgamoneyApiPropriedade property;
	
	
	@Bean
	public AmazonS3 amazonS3() {
		//DEFININDO AS CREDENCIAIS
		AWSCredentials credenciais = new BasicAWSCredentials(
				property.getS3().getAccessKeyId(), property.getS3().getSecretAccessKey());
				
		
		//PEGANDO AS CREDENCIAIS
		AmazonS3 amazonS3 = AmazonS3ClientBuilder.standard()
				.withCredentials(new AWSStaticCredentialsProvider(credenciais))
				.withRegion(Regions.US_EAST_1)
				.build();
		
		// APENDICE AULA 22.31 - criando bucket no S3, essa forma é para criar caso o bucket já não esteja criado no S3
		if(!amazonS3.doesBucketExistV2(property.getS3().getBucket())){
		   
			amazonS3.createBucket(new CreateBucketRequest(property.getS3().getBucket()));
			
			//CRIANDO UM REGRA PARA EXPIRAÇÃO DO ARQUIVO NO BUCKET DA AMAZON
			BucketLifecycleConfiguration.Rule regraExpiracao =
						new BucketLifecycleConfiguration.Rule()
						.withId("Regra de expiração de arquivos temporários")
						.withFilter(new LifecycleFilter(
								new LifecycleTagPredicate(new Tag("expirar","true"))))
						.withExpirationInDays(1) //OS ARQUIVOS COM A TAG "expirar" serão excluidos após 1 dia
						.withStatus(BucketLifecycleConfiguration.ENABLED);
			
			//CRIANDO O OBJETO CONFIGURAÇÃO PRA USAR A REGRA
			BucketLifecycleConfiguration configuration = new BucketLifecycleConfiguration()
						.withRules(regraExpiracao);
			
			//SETANDO O BUCKET E O ARQUIVO DE CONFIGURAÇÃO COM A REGRA CRIADA
			amazonS3.setBucketLifecycleConfiguration(property.getS3().getBucket(),
					 configuration);
		}
		
		return amazonS3;
		
		}
	
}
