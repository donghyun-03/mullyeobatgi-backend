package com.danditon.mullyeobatgi.payment;

import com.danditon.mullyeobatgi.auth.AuthUser;
import com.danditon.mullyeobatgi.common.ApiException;
import com.danditon.mullyeobatgi.succession.Succession;
import com.danditon.mullyeobatgi.succession.SuccessionAccess;
import com.danditon.mullyeobatgi.succession.SuccessionProgress;
import com.danditon.mullyeobatgi.user.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * ⑧ 납입 관리. 청년이 낸 것을 기록하고 사장님이 받았다고 확인한다.
 */
@Service
@RequiredArgsConstructor
public class PaymentService {

	private final SuccessionAccess access;
	private final PaymentInstallmentRepository payments;
	private final SuccessionProgress progress;
	private final Clock clock;

	@Transactional(readOnly = true)
	public PaymentsResponse view(Long successionId, AuthUser me) {
		access.get(successionId, me);
		return response(successionId);
	}

	@Transactional
	public PaymentsResponse report(Long successionId, Long installmentId, AuthUser me, LocalDate paidDate,
			String memo) {
		Succession succession = access.get(successionId, me);
		access.requireSide(succession, me, Role.YOUTH);
		LocalDate date = paidDate == null ? LocalDate.now(clock) : paidDate;
		if (date.isAfter(LocalDate.now(clock))) {
			throw ApiException.badRequest("INVALID_DATE", "보낸 날짜는 오늘이나 그 이전이어야 해요");
		}
		find(successionId, installmentId).report(date, memo, LocalDateTime.now(clock));
		return response(successionId);
	}

	@Transactional
	public PaymentsResponse confirm(Long successionId, Long installmentId, AuthUser me) {
		Succession succession = access.get(successionId, me);
		access.requireSide(succession, me, Role.OWNER);
		find(successionId, installmentId).confirm(LocalDateTime.now(clock));
		payments.flush();
		progress.refresh(succession);
		return response(successionId);
	}

	private PaymentInstallment find(Long successionId, Long installmentId) {
		return payments.findById(installmentId)
				.filter(p -> p.getSuccession().getId().equals(successionId))
				.orElseThrow(() -> ApiException.notFound("납입 항목을 찾을 수 없어요"));
	}

	private PaymentsResponse response(Long successionId) {
		List<PaymentInstallment> list = payments.findBySuccessionIdOrderBySeq(successionId);
		if (list.isEmpty()) {
			throw ApiException.notFound("두 분이 승계 계획에 동의하면 납입 일정이 만들어져요");
		}
		LocalDate today = LocalDate.now(clock);
		int total = list.stream().mapToInt(PaymentInstallment::getAmount).sum();
		int confirmed = sum(list, PaymentStatus.CONFIRMED);
		int waiting = sum(list, PaymentStatus.REPORTED);
		List<PaymentsResponse.Installment> views = list.stream()
				.map(p -> new PaymentsResponse.Installment(p.getId(), p.getSeq(), p.getLabel(), p.getAmount(),
						p.getDueDate(), p.getStatus(), p.getPaidDate(), p.getMemo(), p.getReportedAt(),
						p.getConfirmedAt(), p.getStatus() == PaymentStatus.SCHEDULED && p.getDueDate().isBefore(today)))
				.toList();
		int percent = total == 0 ? 0 : Math.round(confirmed * 100f / total);
		return new PaymentsResponse(total, confirmed, waiting, total - confirmed, percent, views,
				PaymentsResponse.NOTICE);
	}

	private static int sum(List<PaymentInstallment> list, PaymentStatus status) {
		return list.stream().filter(p -> p.getStatus() == status).mapToInt(PaymentInstallment::getAmount).sum();
	}

}
