package com.example.algamoney.api.resource;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.algamoney.api.config.propriedade.AlgamoneyApiPropriedade;

/*CLASSE PARA IMPLEMENTAR O LOGOUT, OU SEJA O CLIENTE IRA CHAMAR E O COOKIE DO REFRESH_TOKEN
 * SERA APAGADO. ALÉM DISSO É IMPORTANTE MANTER O ACESS_TOKEN COM UM TEMPO DE EXPIRAÇÃO PEQUENO*/

@RestController
@RequestMapping("/tokens")
public class TokenResource {
	
	@Autowired
	private AlgamoneyApiPropriedade algamoneyApiPropriedade;

	@DeleteMapping("/revoke")
	public void revoke(HttpServletRequest req, HttpServletResponse resp) {
		
		Cookie cookie = new Cookie("refreshToken", null);
		cookie.setHttpOnly(true);
		cookie.setSecure(algamoneyApiPropriedade.getSeguranca().isEnableHttps()); 
		cookie.setPath(req.getContextPath()+"/oauth/token");
		cookie.setMaxAge(0);
		
		resp.addCookie(cookie);
		resp.setStatus(HttpStatus.NO_CONTENT.value());
	}
	
}
