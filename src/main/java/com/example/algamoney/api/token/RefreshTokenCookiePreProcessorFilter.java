package com.example.algamoney.api.token;

import java.io.IOException;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.apache.catalina.util.ParameterMap;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE) //Filtro com uma prioridade muito alta
public class RefreshTokenCookiePreProcessorFilter implements Filter{

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		
		HttpServletRequest req = (HttpServletRequest) request;
		
		//AQUI FAZ O TESTE SE O QUE FOI FILTRADO ESTA REALMENTE PEGANDO O REFRESH_TOKEN
		if ("/oauth/token".equalsIgnoreCase(req.getRequestURI()) 
				&& "refresh_token".equals(req.getParameter("grant_type"))
				&& req.getCookies() != null) {
			for (Cookie cookie : req.getCookies()) {  //GARANTINDO QUE EXISTE UM REFRESH_TOKEN VAMOS PEGAR O COOKIE
				if (cookie.getName().equals("refreshToken")) {
					String refreshToken = cookie.getValue();  //AQUI PEGO O VALOR DO REFRESH_TOKEN E COLOCO EM UM STRING
					req = new MyServletRequestWrapper(req, refreshToken);  //AGORA COM ESSA CLASSE ESTA INCLUINDO O VALOR DO REFRESHTOKEN EM UMA NOVA REQUISIÇÃO PARA DEVOLVER A APLICAÇÃO
				}
			}
		}
	
		chain.doFilter(req, response);
	}
	
	//CLASSE PARA REPARAR UMA NOVA REQUISIÇÃO QUE CONTENHA O COOKIE RECUPERADO
	static class MyServletRequestWrapper extends HttpServletRequestWrapper {

		private String refreshToken;
		
		public MyServletRequestWrapper(HttpServletRequest request, String refreshToken) {
			super(request);
			this.refreshToken = refreshToken;
		}
		
		@Override
		public Map<String, String[]> getParameterMap() {
			ParameterMap<String, String[]> map = new ParameterMap<>(getRequest().getParameterMap());
			map.put("refresh_token", new String[] { refreshToken });
			map.setLocked(true);
			return map;
		}
		
	}

	
	
}
