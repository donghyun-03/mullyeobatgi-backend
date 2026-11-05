package com.danditon.mullyeobatgi.payment;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * ⑧ 납입 현황. 금액은 만 원 단위. 사장님이 확인한 금액만 낸 금액으로 센다.
 */
@Schema(description = "납입 현황. 금액은 만 원 단위. 사장님이 확인한 금액만 낸 금액(confirmedAmount)으로 세요")
public record PaymentsResponse(
		int total,
		int confirmedAmount,
		int waitingConfirmAmount,
		int remainingAmount,
		int paidPercent,
		List<Installment> installments,
		String notice) {

	public static final String NOTICE = "물려받기는 돈을 직접 보내지 않아요. 계좌로 보낸 뒤 기록하면 사장님이 받았는지 확인해요.";

	public record Installment(Long id, int seq, String label, int amount, LocalDate dueDate, PaymentStatus status,
			LocalDate paidDate, String memo, LocalDateTime reportedAt, LocalDateTime confirmedAt, boolean overdue) {
	}

}
