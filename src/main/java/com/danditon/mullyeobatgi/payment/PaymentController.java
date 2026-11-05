package com.danditon.mullyeobatgi.payment;

import com.danditon.mullyeobatgi.auth.AuthUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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
 * ⑧ 납입 관리
 */
@Tag(name = "⑧ 납입 관리")
@RestController
@RequestMapping("/api/successions/{successionId}/payments")
@RequiredArgsConstructor
public class PaymentController {

	private final PaymentService paymentService;

	@Schema(description = "납입 기록 (본문 생략 가능)")
	public record ReportRequest(
			@Schema(description = "보낸 날짜. 비우면 오늘. 오늘 이후는 안 돼요", example = "2027-03-02")
			LocalDate paidDate,
			@Schema(description = "메모", example = "국민은행 계좌 이체")
			@Size(max = 300) String memo) {
	}

	@Operation(summary = "납입 현황 보기",
			description = """
					두 사람이 계획에 동의하면 만들어지는 인수 금액 납입 일정이에요. 금액은 만 원 단위예요.

					| seq | label | 금액 | 낼 날 |
					|---|---|---|---|
					| 1 | 계약금 | 10% | 계획 동의 마감일 |
					| 2 | 중도금 1차 | 30% | 수습 2단계 시작일 |
					| 3 | 중도금 2차 | 30% | 수습 마지막 날 |
					| 4 | 잔금 | 나머지 | 은퇴 희망일 |

					- `status`: `SCHEDULED` 아직 안 냄, `REPORTED` 청년이 냈다고 기록함(사장님 확인 대기), `CONFIRMED` 사장님이 받았다고 확인함
					- `confirmedAmount`: 사장님이 확인한 금액만 낸 금액으로 세요. `remainingAmount` = 전체 - 확인된 금액
					- `waitingConfirmAmount`: 기록했지만 아직 확인 안 된 금액
					- `overdue`: 낼 날이 지났는데 아직 기록이 없음
					- `notice`: 화면에 함께 보여 줄 안내 (물려받기는 돈을 직접 옮기지 않아요)

					아직 납입 일정이 없으면 404 예요.""")
	@GetMapping
	public PaymentsResponse view(@Parameter(hidden = true) @AuthenticationPrincipal AuthUser me,
			@Parameter(description = "승계 id") @PathVariable Long successionId) {
		return paymentService.view(successionId, me);
	}

	@Operation(summary = "냈다고 기록 (청년)",
			description = """
					계좌로 돈을 보낸 뒤 기록해요. 사장님 홈에 `PAYMENT_CONFIRM` 할 일로 나타나요.
					`REPORTED` 상태에서 다시 부르면 날짜와 메모를 고쳐요.

					오류: 403 청년이 아님, 400 `INVALID_DATE` 오늘 이후 날짜, 409 `PAYMENT_CONFIRMED` 이미 확인된 납입""")
	@PostMapping("/{installmentId}/report")
	public PaymentsResponse report(@Parameter(hidden = true) @AuthenticationPrincipal AuthUser me,
			@Parameter(description = "승계 id") @PathVariable Long successionId,
			@Parameter(description = "납입 id (installments[].id)") @PathVariable Long installmentId,
			@Valid @RequestBody(required = false) ReportRequest body) {
		return paymentService.report(successionId, installmentId, me, body == null ? null : body.paidDate(),
				body == null ? null : body.memo());
	}

	@Operation(summary = "받았다고 확인 (사장님)",
			description = """
					청년이 기록한 납입을 받았다고 확인해요.
					수습이 모두 끝난(`HANDOVER`) 뒤 **4번 모두 확인되면** 승계가 `COMPLETED`(완료)가 돼요.

					오류: 403 사장님이 아님, 409 `PAYMENT_NOT_REPORTED` 청년이 아직 기록하지 않음""")
	@PostMapping("/{installmentId}/confirm")
	public PaymentsResponse confirm(@Parameter(hidden = true) @AuthenticationPrincipal AuthUser me,
			@Parameter(description = "승계 id") @PathVariable Long successionId,
			@Parameter(description = "납입 id (installments[].id)") @PathVariable Long installmentId) {
		return paymentService.confirm(successionId, installmentId, me);
	}

}
