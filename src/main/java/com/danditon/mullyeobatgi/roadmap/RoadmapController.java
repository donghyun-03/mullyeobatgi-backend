package com.danditon.mullyeobatgi.roadmap;

import com.danditon.mullyeobatgi.auth.AuthUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * ⑦ 수습 로드맵
 */
@Tag(name = "⑦ 수습 로드맵")
@RestController
@RequestMapping("/api/successions/{successionId}/roadmap")
@RequiredArgsConstructor
public class RoadmapController {

	private final RoadmapService roadmapService;

	@Schema(description = "완료 요청 (본문 생략 가능)")
	public record RequestBodyMemo(
			@Schema(description = "사장님께 남기는 말", example = "오늘 점심 장사 혼자 봤어요")
			@Size(max = 300) String memo) {
	}

	@Schema(description = "다시 해 보기 요청")
	public record SendBackRequest(
			@Schema(description = "다시 해 보라는 이유. 청년 홈에 그대로 보여요", example = "간을 조금 더 맞춰 보자")
			@NotBlank @Size(max = 300) String reason) {
	}

	@Operation(summary = "수습 로드맵 보기",
			description = """
					두 사람이 계획에 동의하면 만들어지는 수습 3단계예요. 단계마다 일이 3개씩 있어요.

					| 단계 | 일 |
					|---|---|
					| 1. 주방 익히기 | 재료 손질과 주방 동선, 거래처 인사, 단골 얼굴과 이름 |
					| 2. 혼자 만들기 | 대표 메뉴 혼자 만들기, 밑재료 준비, 점심 장사 혼자 보기 |
					| 3. 가게 운영 | 장부·정산 같이 보기, 거래처 주문, 하루 운영 맡기 |

					- 단계 `state`: `DONE` 모두 승인됨, `NOW` 지금 단계(하나만), `LOCKED` 앞 단계를 마쳐야 열림
					- 일 `status`: `TODO` 할 일(다시 해 보기 포함, 이때 `feedback` 에 이유), `REQUESTED` 승인 기다리는 중, `APPROVED` 승인됨
					- `currentStage`: 지금 단계 번호 (모두 끝나면 null)
					- `waitingApprovalCount`: 사장님 승인을 기다리는 일 개수

					아직 로드맵이 없으면 404 예요.""")
	@GetMapping
	public RoadmapResponse view(@Parameter(hidden = true) @AuthenticationPrincipal AuthUser me,
			@Parameter(description = "승계 id") @PathVariable Long successionId) {
		return roadmapService.view(successionId, me);
	}

	@Operation(summary = "완료 요청 (청년)",
			description = """
					마친 일의 완료를 사장님께 요청해요. 사장님 홈에 `ROADMAP_APPROVE` 할 일로 나타나요.
					**지금 단계(`NOW`)의 일만** 요청할 수 있어요.

					오류: 403 청년이 아님, 409 `STAGE_LOCKED` 앞 단계가 안 끝남, 409 `TASK_NOT_TODO` 이미 요청했거나 승인됨,
					409 `NOT_APPRENTICING` 수습 중이 아님""")
	@PostMapping("/tasks/{taskId}/request")
	public RoadmapResponse requestComplete(@Parameter(hidden = true) @AuthenticationPrincipal AuthUser me,
			@Parameter(description = "승계 id") @PathVariable Long successionId,
			@Parameter(description = "로드맵 일 id") @PathVariable Long taskId,
			@Valid @RequestBody(required = false) RequestBodyMemo body) {
		return roadmapService.requestComplete(successionId, taskId, me, body == null ? null : body.memo());
	}

	@Operation(summary = "승인 (사장님)",
			description = """
					청년이 완료를 요청한 일을 승인해요. 한 단계의 일이 모두 승인되면 다음 단계가 열려요.
					**9개 일이 모두 승인되면** 승계가 `HANDOVER`(운영권 넘겨받기)로 넘어가요.
					이때 납입까지 모두 확인돼 있으면 바로 `COMPLETED` 가 돼요.

					오류: 403 사장님이 아님, 409 `TASK_NOT_REQUESTED` 완료 요청이 온 일이 아님""")
	@PostMapping("/tasks/{taskId}/approve")
	public RoadmapResponse approve(@Parameter(hidden = true) @AuthenticationPrincipal AuthUser me,
			@Parameter(description = "승계 id") @PathVariable Long successionId,
			@Parameter(description = "로드맵 일 id") @PathVariable Long taskId) {
		return roadmapService.approve(successionId, taskId, me);
	}

	@Operation(summary = "다시 해 보기 (사장님)",
			description = """
					완료 요청을 돌려보내요. 일이 `TODO` 로 돌아가고 이유가 `feedback` 에 남아요.
					청년 홈에 `ROADMAP_RETRY` 할 일로 나타나고, 다시 연습한 뒤 완료를 요청하면 돼요.

					오류: 403 사장님이 아님, 409 `TASK_NOT_REQUESTED` 완료 요청이 온 일이 아님""")
	@PostMapping("/tasks/{taskId}/send-back")
	public RoadmapResponse sendBack(@Parameter(hidden = true) @AuthenticationPrincipal AuthUser me,
			@Parameter(description = "승계 id") @PathVariable Long successionId,
			@Parameter(description = "로드맵 일 id") @PathVariable Long taskId,
			@Valid @RequestBody SendBackRequest body) {
		return roadmapService.sendBack(successionId, taskId, me, body.reason());
	}

}
