package com.danditon.mullyeobatgi.schedule;

import com.danditon.mullyeobatgi.auth.AuthUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

/**
 * ④ 가일정
 */
@Tag(name = "④ 가일정")
@RestController
@RequestMapping("/api/successions/{successionId}/schedule")
@RequiredArgsConstructor
public class ScheduleController {

	private final ScheduleService scheduleService;

	@Schema(description = "일정 변경 요청")
	public record ChangeRequestBody(
			@Schema(description = "새 은퇴 희망일. 오늘 이후여야 해요", example = "2027-09-30")
			@NotNull LocalDate retireDate,
			@Schema(description = "바꾸고 싶은 이유", example = "조금 더 가게를 하고 싶어요")
			@Size(max = 300) String reason) {
	}

	@Operation(summary = "승계 가일정 보기",
			description = """
					사장님의 **은퇴 희망일에서 거꾸로 계산한** 승계 일정이에요. 6줄로 나와요.

					| seq | code | 내용 | 날짜 계산 |
					|---|---|---|---|
					| 1 | `ACCEPT` | 제안 수락 | 수락한 날 |
					| 2 | `CHECKLIST` | 체크리스트 확인 | 계획 마감 5일 전까지 |
					| 3 | `PLAN` | AI 승계 계획 동의 · 계약금 | 수습 시작 12일 전까지 |
					| 4 | `APPRENTICE` | 수습 N개월 | 운영권 넘기기 시작 전 N개월 |
					| 5 | `HANDOVER` | 운영권 넘겨받기 | 은퇴일 2개월 전부터 |
					| 6 | `COMPLETE` | 잔금 납입 · 승계 완료 | 은퇴 희망일 |

					- `state`: `DONE` 끝남, `NOW` 지금 할 일(하나만), `UPCOMING` 앞으로 할 일. 날짜가 아니라 승계 단계로 정해요.
					- `daysLeft`: 은퇴 희망일까지 남은 날
					- `warnings`: 체크리스트 기간이 일주일보다 짧거나 수습 시작일이 지났을 때 안내 문장
					- `changeRequests`: 일정 변경 요청 목록 (최신순). `mine` 이 true 면 내가 보낸 요청이에요.""")
	@GetMapping
	public ScheduleResponse view(@Parameter(hidden = true) @AuthenticationPrincipal AuthUser me,
			@Parameter(description = "승계 id") @PathVariable Long successionId) {
		return scheduleService.view(successionId, me);
	}

	@Operation(summary = "일정 변경 요청",
			description = """
					은퇴 희망일을 바꾸자고 **상대방에게 요청**해요. 혼자서는 바꿀 수 없고, 상대방이 승인해야 반영돼요.

					오류: 400 `INVALID_DATE` 오늘 이전 날짜, 409 `REQUEST_PENDING` 답을 기다리는 요청이 이미 있음,
					409 `SUCCESSION_COMPLETED` 끝난 승계""")
	@PostMapping("/change-requests")
	public ScheduleResponse requestChange(@Parameter(hidden = true) @AuthenticationPrincipal AuthUser me,
			@Parameter(description = "승계 id") @PathVariable Long successionId,
			@Valid @RequestBody ChangeRequestBody body) {
		return scheduleService.requestChange(successionId, me, body.retireDate(), body.reason());
	}

	@Operation(summary = "일정 변경 승인",
			description = """
					상대방이 보낸 일정 변경 요청을 승인해요. 승인하면 한 번에 다시 맞춰져요.
					- 은퇴 희망일과 가일정 6줄
					- 수습 로드맵 단계별 기간 (만들어져 있으면)
					- 아직 사장님이 확인하지 않은 납입 날짜 (만들어져 있으면)

					오류: 403 내가 보낸 요청은 승인할 수 없음, 409 `REQUEST_DECIDED` 이미 답한 요청""")
	@PostMapping("/change-requests/{requestId}/approve")
	public ScheduleResponse approve(@Parameter(hidden = true) @AuthenticationPrincipal AuthUser me,
			@Parameter(description = "승계 id") @PathVariable Long successionId,
			@Parameter(description = "변경 요청 id") @PathVariable Long requestId) {
		return scheduleService.decide(successionId, requestId, me, true);
	}

	@Operation(summary = "일정 변경 거절",
			description = "상대방이 보낸 일정 변경 요청을 거절해요. 일정은 그대로예요. 오류는 승인과 같아요.")
	@PostMapping("/change-requests/{requestId}/reject")
	public ScheduleResponse reject(@Parameter(hidden = true) @AuthenticationPrincipal AuthUser me,
			@Parameter(description = "승계 id") @PathVariable Long successionId,
			@Parameter(description = "변경 요청 id") @PathVariable Long requestId) {
		return scheduleService.decide(successionId, requestId, me, false);
	}

}
