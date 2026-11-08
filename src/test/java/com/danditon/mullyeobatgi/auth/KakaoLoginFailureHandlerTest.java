package com.danditon.mullyeobatgi.auth;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;

import static org.assertj.core.api.Assertions.assertThat;

class KakaoLoginFailureHandlerTest {

	private final KakaoLoginFailureHandler handler = new KakaoLoginFailureHandler("https://front.example");

	@Test
	void sendsOAuthErrorCodeToFrontendCallback() throws Exception {
		MockHttpServletResponse response = new MockHttpServletResponse();
		handler.onAuthenticationFailure(new MockHttpServletRequest(), response,
				new OAuth2AuthenticationException(new OAuth2Error("access_denied")));
		assertThat(response.getRedirectedUrl()).isEqualTo("https://front.example/oauth/callback#error=access_denied");
	}

	@Test
	void usesGenericCodeForOtherFailures() throws Exception {
		MockHttpServletResponse response = new MockHttpServletResponse();
		handler.onAuthenticationFailure(new MockHttpServletRequest(), response, new BadCredentialsException("x"));
		assertThat(response.getRedirectedUrl()).isEqualTo("https://front.example/oauth/callback#error=login_failed");
	}

}
