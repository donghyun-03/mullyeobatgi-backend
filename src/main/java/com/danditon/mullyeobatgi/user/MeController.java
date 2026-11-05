package com.danditon.mullyeobatgi.user;

import com.danditon.mullyeobatgi.auth.AuthUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "0. 로그인·내 정보")
@RestController
@RequestMapping("/api/me")
@RequiredArgsConstructor
public class MeController {

	private final MeService meService;

	@Schema(description = "역할 선택 요청")
	public record RoleRequest(
			@Schema(description = "OWNER: 가게를 물려주는 사장님, YOUTH: 가게를 이어받는 청년", example = "YOUTH")
			@NotNull Role role) {
	}

	@Operation(summary = "내 정보",
			description = """
					로그인한 사용자와, 앱을 열었을 때 어느 화면으로 보낼지 정하는 데 필요한 상태를 돌려줘요.

					- `role` 이 비어 있으면 → 역할 선택 화면
					- 사장님인데 `hasShop` 이 false 면 → 가게 등록 화면
					- 청년인데 `hasYouthProfile` 이 false 면 → 청년 등록 화면
					- `successionId` 가 있으면 → 홈(승계 요약). 없으면 사장님은 매칭, 청년은 받은 제안 화면""")
	@GetMapping
	public MeResponse me(@Parameter(hidden = true) @AuthenticationPrincipal AuthUser me) {
		return meService.me(me.id());
	}

	@Operation(summary = "역할 선택",
			description = """
					사장님인지 청년인지 골라요. 카카오로 처음 들어온 사용자는 역할이 비어 있어서 이 API를 먼저 불러야 해요.

					- 가게 정보나 청년 정보를 등록한 뒤에는 다른 역할로 바꿀 수 없어요 (409 `ROLE_LOCKED`).
					- 같은 역할을 다시 고르는 건 언제나 괜찮아요.""")
	@PutMapping("/role")
	public MeResponse chooseRole(@Parameter(hidden = true) @AuthenticationPrincipal AuthUser me,
			@Valid @RequestBody RoleRequest request) {
		return meService.chooseRole(me.id(), request.role());
	}

}
