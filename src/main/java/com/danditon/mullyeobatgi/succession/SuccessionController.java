package com.danditon.mullyeobatgi.succession;

import com.danditon.mullyeobatgi.auth.AuthUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 홈 화면의 승계 요약과 확인할 일.
 */
@Tag(name = "홈 · 승계 요약")
@RestController
@RequestMapping("/api/successions")
@RequiredArgsConstructor
public class SuccessionController {

	private final SuccessionService successionService;

	private static final String SUMMARY_DESCRIPTION = """
			홈 화면에 필요한 승계 요약을 돌려줘요.

			- `status` / `statusLabel`: 지금 단계 (CHECKLIST 체크리스트 확인 → PLANNING 승계 계획 동의 →
			  APPRENTICE 수습 → HANDOVER 운영권 넘겨받기 → COMPLETED 승계 완료)
			- `progressPercent`: 진행률 0~100
			- `checklist`, `roadmap`: 완료 개수/전체 개수. `payments`: 확인된 금액/전체 인수 금액 (만 원)
			- `nextStep`: 다음 일정과 마감일 (완료되면 null)
			- `planAgreedDate`: 두 사람이 계획에 동의한 날 (아직이면 null). 홈의 "2026.12 계획 동의" 표시
			- `roadmapCurrentStage`: 수습 로드맵 지금 단계 번호, `roadmapWaitingApproval`: 사장님 승인을 기다리는 일 개수.
			  홈의 "2단계 진행 중 · 승인 대기 1개" 표시
			- **`pendingActions`: 지금 내 차례인 할 일.** 사장님과 청년에게 서로 다르게 나와요.

			| type | 누구 | 뜻 | targetId |
			|---|---|---|---|
			| `SCHEDULE_CHANGE` | 둘 다 | 상대방이 일정 변경을 요청함 | 변경 요청 id |
			| `CHECKLIST` | 둘 다 | 내가 확인 안 한 체크리스트가 남음 | - |
			| `PLAN_CREATE` | 둘 다 | 체크리스트 완료, 계획 초안을 만들 차례 | - |
			| `PLAN_AGREE` | 둘 다 | 내가 아직 계획에 동의 안 함 | 계획 id |
			| `ROADMAP_APPROVE` | 사장님 | 청년이 수습 일 완료를 요청함 | 로드맵 일 id |
			| `ROADMAP_RETRY` | 청년 | 사장님이 다시 해 보라고 함 | 로드맵 일 id |
			| `PAYMENT_CONFIRM` | 사장님 | 청년이 납입을 기록함 | 납입 id |
			| `PAYMENT_DUE` | 청년 | 7일 안에 낼 금액이 있음 | 납입 id |""";

	@Operation(summary = "내 승계 요약 (홈)",
			description = SUMMARY_DESCRIPTION + "\n\n내가 참여한 가장 최근 승계를 돌려줘요. 아직 승계가 없으면 404 예요.")
	@GetMapping("/current")
	public SuccessionSummary current(@Parameter(hidden = true) @AuthenticationPrincipal AuthUser me) {
		return successionService.current(me);
	}

	@Operation(summary = "승계 요약",
			description = SUMMARY_DESCRIPTION + "\n\n승계에 참여한 사장님과 청년만 볼 수 있어요 (그 외 403).")
	@GetMapping("/{successionId}")
	public SuccessionSummary get(@Parameter(hidden = true) @AuthenticationPrincipal AuthUser me,
			@Parameter(description = "승계 id (제안 수락 응답의 successionId)") @PathVariable Long successionId) {
		return successionService.get(successionId, me);
	}

}
