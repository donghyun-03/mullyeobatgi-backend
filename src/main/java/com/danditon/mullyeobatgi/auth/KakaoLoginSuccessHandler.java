package com.danditon.mullyeobatgi.auth;

import com.danditon.mullyeobatgi.user.User;
import com.danditon.mullyeobatgi.user.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 카카오 로그인이 끝나면 우리 토큰을 발급해 프론트엔드로 돌려보낸다.
 * 토큰은 서버 로그에 남지 않도록 URL 프래그먼트(#)에 담는다.
 */
@Component
public class KakaoLoginSuccessHandler implements AuthenticationSuccessHandler {

	private final UserRepository users;
	private final TokenService tokenService;
	private final Clock clock;
	private final String frontendUrl;

	public KakaoLoginSuccessHandler(UserRepository users, TokenService tokenService, Clock clock,
			@Value("${app.frontend-url}") String frontendUrl) {
		this.users = users;
		this.tokenService = tokenService;
		this.clock = clock;
		this.frontendUrl = frontendUrl;
	}

	@Override
	@Transactional
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
			Authentication authentication) throws IOException {
		OAuth2User kakaoUser = (OAuth2User) authentication.getPrincipal();
		Long kakaoId = Long.valueOf(String.valueOf(kakaoUser.getAttributes().get("id")));
		User user = users.findByKakaoId(kakaoId)
				.orElseGet(() -> users.save(new User(nickname(kakaoUser), null, kakaoId, LocalDateTime.now(clock))));
		String token = tokenService.issue(user);
		response.sendRedirect(frontendUrl + "/oauth/callback#token=" + token);
	}

	private String nickname(OAuth2User kakaoUser) {
		Object properties = kakaoUser.getAttributes().get("properties");
		if (properties instanceof Map<?, ?> map && map.get("nickname") != null) {
			return String.valueOf(map.get("nickname"));
		}
		return "카카오 사용자";
	}

}
