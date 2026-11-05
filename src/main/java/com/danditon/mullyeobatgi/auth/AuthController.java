package com.danditon.mullyeobatgi.auth;

import com.danditon.mullyeobatgi.common.ApiException;
import com.danditon.mullyeobatgi.user.MeResponse;
import com.danditon.mullyeobatgi.user.MeService;
import com.danditon.mullyeobatgi.user.Role;
import com.danditon.mullyeobatgi.user.User;
import com.danditon.mullyeobatgi.user.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Clock;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

	private final UserRepository users;
	private final TokenService tokenService;
	private final MeService meService;
	private final Clock clock;
	private final boolean devLoginEnabled;

	public AuthController(UserRepository users, TokenService tokenService, MeService meService, Clock clock,
			@Value("${app.auth.dev-login-enabled}") boolean devLoginEnabled) {
		this.users = users;
		this.tokenService = tokenService;
		this.meService = meService;
		this.clock = clock;
		this.devLoginEnabled = devLoginEnabled;
	}

	public record DevLoginRequest(@NotBlank @Size(max = 50) String name, Role role) {
	}

	public record LoginResponse(String token, MeResponse me) {
	}

	/**
	 * 시연·개발용 로그인. 같은 이름과 역할이면 같은 사용자로 들어간다.
	 */
	@PostMapping("/dev-login")
	@Transactional
	public LoginResponse devLogin(@Valid @RequestBody DevLoginRequest request) {
		if (!devLoginEnabled) {
			throw ApiException.notFound("개발용 로그인이 꺼져 있어요");
		}
		User user = users.findFirstByNameAndRole(request.name(), request.role())
				.orElseGet(() -> users.save(new User(request.name(), request.role(), null, LocalDateTime.now(clock))));
		return new LoginResponse(tokenService.issue(user), meService.me(user.getId()));
	}

	@PostMapping("/logout")
	public ResponseEntity<Void> logout(HttpServletRequest request) {
		String token = TokenAuthenticationFilter.extractToken(request);
		if (token != null) {
			tokenService.revoke(token);
		}
		return ResponseEntity.noContent().build();
	}

}
