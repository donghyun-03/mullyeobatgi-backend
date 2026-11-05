package com.danditon.mullyeobatgi.payment;

import com.danditon.mullyeobatgi.auth.AuthUser;
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
@RestController
@RequestMapping("/api/successions/{successionId}/payments")
@RequiredArgsConstructor
public class PaymentController {

	private final PaymentService paymentService;

	/** paidDate 를 비우면 오늘로 기록한다 */
	public record ReportRequest(LocalDate paidDate, @Size(max = 300) String memo) {
	}

	@GetMapping
	public PaymentsResponse view(@AuthenticationPrincipal AuthUser me, @PathVariable Long successionId) {
		return paymentService.view(successionId, me);
	}

	/** 청년: 냈다고 기록 */
	@PostMapping("/{installmentId}/report")
	public PaymentsResponse report(@AuthenticationPrincipal AuthUser me, @PathVariable Long successionId,
			@PathVariable Long installmentId, @Valid @RequestBody(required = false) ReportRequest body) {
		return paymentService.report(successionId, installmentId, me, body == null ? null : body.paidDate(),
				body == null ? null : body.memo());
	}

	/** 사장님: 받았다고 확인 */
	@PostMapping("/{installmentId}/confirm")
	public PaymentsResponse confirm(@AuthenticationPrincipal AuthUser me, @PathVariable Long successionId,
			@PathVariable Long installmentId) {
		return paymentService.confirm(successionId, installmentId, me);
	}

}
