package com.example.algamoney.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.method.configuration.GlobalMethodSecurityConfiguration;
import org.springframework.security.oauth2.provider.expression.OAuth2MethodSecurityExpressionHandler;
import org.springframework.web.bind.annotation.PostMapping;

/*CLASSE PARA QUE SEJA POSSIVEL USAR O CONTROLE DE ROLES NOS RESOURCES
 * DESSA FORMA CONSEGUIMOS TER OS CONTROLE DE METODOS NOS RESOURCE
 * @PostMapping
	@PreAuthorize("hasAuthority('ROLE_CADASTRAR_CATEGORIA')")*/

//@Profile("oauth-security")
@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class MethodSecurityConfiguration extends GlobalMethodSecurityConfiguration{

	
	@Bean   
	@Override
	    public MethodSecurityExpressionHandler createExpressionHandler() {

	        return new OAuth2MethodSecurityExpressionHandler();
	    }
	   
	   
}
