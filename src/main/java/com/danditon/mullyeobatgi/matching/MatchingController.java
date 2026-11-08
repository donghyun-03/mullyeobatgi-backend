package com.danditon.mullyeobatgi.matching;

import com.danditon.mullyeobatgi.auth.AuthUser;
import com.danditon.mullyeobatgi.user.Role;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * ② 매칭
 */
@Tag(name = "② 매칭")
@RestController
@RequiredArgsConstructor
public class MatchingController {

	private final MatchingService matchingService;

	@Operation(summary = "내 가게와 맞는 청년 후보 (사장님)",
			description = """
					등록된 청년을 적합도 점수가 높은 순으로 최대 20명 돌려줘요. 다른 가게와 승계 중인 청년은 빠져요.

					- `score`: 100점 만점 (경력 30 + 자금 30 + 조건 40). 같은 조건이면 언제나 같은 점수가 나와요.
					- `breakdown`: 경력·자금·조건 항목별 점수와 수준(`높음` 80% 이상, `보통` 50% 이상, `낮음`)
					- `reasons`: "이래서 맞아요" 목록. `GOOD` 이 먼저, 주의할 점 `CAUTION` 이 뒤에 와요.
					  청년이 새벽 근무 여부를 안 적었으면 "새벽 근무가 가능한지 아직 몰라요"가 붙어요.
					- `aiComment`: AI 키가 있을 때만 상위 3명에게 붙는 한두 문장 설명. 키가 없으면 null 이에요.
					- `offerId`, `offerStatus`: 이 청년에게 보낸 가장 최근 제안. 없으면 null.
					  `PROPOSED` 면 답을 기다리는 중이라 다시 제안할 수 없어요.

					가게를 등록하지 않았으면 404 예요.""")
	@GetMapping("/api/owner/matches")
	public List<YouthCandidateResponse> youthCandidates(@Parameter(hidden = true) @AuthenticationPrincipal AuthUser me) {
		me.requireRole(Role.OWNER);
		return matchingService.candidatesFor(me.id());
	}

	@Operation(summary = "나와 맞는 가게 (청년)",
			description = """
					등록된 가게를 적합도 점수가 높은 순으로 최대 20곳 돌려줘요. 이미 승계 중인 가게는 빠져요.
					점수 계산은 사장님 쪽과 같아요. 제안은 사장님이 보내므로 청년은 둘러보기만 해요.

					청년 정보를 등록하지 않았으면 404 예요.""")
	@GetMapping("/api/youth/matches")
	public List<ShopCandidateResponse> shopCandidates(@Parameter(hidden = true) @AuthenticationPrincipal AuthUser me) {
		me.requireRole(Role.YOUTH);
		return matchingService.shopsFor(me.id());
	}

}
