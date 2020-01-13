package com.springboot.security;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

public class LoginSuccessForOAauthHandler implements AuthenticationSuccessHandler {

	Logger logger = LoggerFactory.getLogger(LoginSuccessForOAauthHandler.class);

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
			Authentication auth) throws IOException, ServletException {
		logger.info(String.format("로그인 핸들러 작동 (username: %s)", auth.getName()) );
		System.out.println("dd: " + request.getParameter("oauth-handler"));
		if(request.getParameter("oauth-handler").equals("naver")) {
			logger.info("assign naver");
			response.sendRedirect("/login/oauth/naver?whereFrom=naverOAuth-s");
		} else {
			response.sendRedirect("/");
		}

	}

}