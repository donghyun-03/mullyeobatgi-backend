package com.danditon.mullyeobatgi.auth;

import com.danditon.mullyeobatgi.common.ApiException;
import com.danditon.mullyeobatgi.user.MeResponse;
import com.danditon.mullyeobatgi.user.MeService;
import com.danditon.mullyeobatgi.user.Role;
import com.danditon.mullyeobatgi.user.User;
import com.danditon.mullyeobatgi.user.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@Tag(name = "0. 로그인·내 정보")
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

	@Schema(description = "개발용 로그인 요청")
	public record DevLoginRequest(
			@Schema(description = "이름. 같은 이름과 역할이면 같은 사용자로 들어가요", example = "김순자")
			@NotBlank @Size(max = 50) String name,
			@Schema(description = "역할. 비우면 역할 없이 만들고 나중에 PUT /api/me/role 로 골라요", example = "OWNER")
			Role role) {
	}

	@Schema(description = "로그인 결과")
	public record LoginResponse(
			@Schema(description = "API 토큰. 이후 요청마다 Authorization: Bearer {token} 으로 보내요. 30일 동안 유효해요")
			String token,
			@Schema(description = "로그인한 사용자 정보")
			MeResponse me) {
	}

	@Operation(summary = "개발용 로그인",
			description = """
					시연과 개발을 위한 로그인이에요. 비밀번호 없이 이름과 역할만으로 들어가요.

					- 같은 이름과 역할의 사용자가 있으면 그 사용자로, 없으면 새로 만들어서 로그인해요.
					- 예시 데이터: 사장님 `김순자`(OWNER), 청년 `이도윤`·`박서연`·`최민재`(YOUTH)
					- 응답의 `token` 을 Swagger 오른쪽 위 **Authorize** 에 넣으면 다른 API를 바로 써 볼 수 있어요.
					- 실제 배포에서 끄려면 `DEV_LOGIN_ENABLED=false`. 꺼져 있으면 404 를 돌려줘요.
					- 카카오 로그인은 `kakao` 프로필로 서버를 켠 뒤 브라우저에서 `/oauth2/authorization/kakao` 로 이동해요.
					  로그인이 끝나면 `{FRONTEND_URL}/oauth/callback#token=...` 으로 돌아와요.""")
	@SecurityRequirements
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

	@Operation(summary = "로그아웃",
			description = "보낸 토큰을 지워서 더 이상 쓸 수 없게 해요. 토큰이 없거나 이미 지워졌어도 204 를 돌려줘요.")
	@SecurityRequirements
	@PostMapping("/logout")
	public ResponseEntity<Void> logout(HttpServletRequest request) {
		String token = TokenAuthenticationFilter.extractToken(request);
		if (token != null) {
			tokenService.revoke(token);
		}
		return ResponseEntity.noContent().build();
	}

}
