package com.danditon.mullyeobatgi.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * 카카오 로그인이 실패하거나 사용자가 취소하면 프론트엔드 콜백으로 오류 코드를 돌려보낸다.
 * 예: {FRONTEND_URL}/oauth/callback#error=access_denied
 */
@Slf4j
@Component
public class KakaoLoginFailureHandler implements AuthenticationFailureHandler {

	private final String frontendUrl;

	public KakaoLoginFailureHandler(@Value("${app.frontend-url}") String frontendUrl) {
		this.frontendUrl = frontendUrl;
	}

	@Override
	public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException exception) throws IOException {
		String code = exception instanceof OAuth2AuthenticationException oauth
				? oauth.getError().getErrorCode()
				: "login_failed";
		log.warn("카카오 로그인 실패: {}", code);
		response.sendRedirect(frontendUrl + "/oauth/callback#error=" + URLEncoder.encode(code, StandardCharsets.UTF_8));
	}

}
