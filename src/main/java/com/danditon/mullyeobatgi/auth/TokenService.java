package com.danditon.mullyeobatgi.auth;

import com.danditon.mullyeobatgi.user.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Optional;

/**
 * 로그인하면 무작위 토큰을 발급하고, 요청의 Authorization: Bearer 토큰으로 사용자를 찾는다.
 */
@Service
public class TokenService {

	private final SecureRandom random = new SecureRandom();
	private final AuthTokenRepository tokens;
	private final Clock clock;
	private final int validDays;

	public TokenService(AuthTokenRepository tokens, Clock clock, @Value("${app.auth.token-valid-days}") int validDays) {
		this.tokens = tokens;
		this.clock = clock;
		this.validDays = validDays;
	}

	@Transactional
	public String issue(User user) {
		byte[] bytes = new byte[32];
		random.nextBytes(bytes);
		String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
		tokens.save(new AuthToken(token, user, LocalDateTime.now(clock).plusDays(validDays)));
		return token;
	}

	@Transactional(readOnly = true)
	public Optional<User> authenticate(String token) {
		return tokens.findWithUser(token)
				.filter(t -> t.getExpiresAt().isAfter(LocalDateTime.now(clock)))
				.map(AuthToken::getUser);
	}

	@Transactional
	public void revoke(String token) {
		tokens.deleteById(token);
	}

}
