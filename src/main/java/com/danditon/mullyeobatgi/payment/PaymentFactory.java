package com.danditon.mullyeobatgi.payment;

import com.danditon.mullyeobatgi.succession.Succession;
import com.danditon.mullyeobatgi.succession.Timeline;

import java.util.List;

/**
 * ⑧ 인수 금액 나눠 내기: 계약금 10%, 중도금 30%씩 두 번, 남은 금액은 잔금.
 */
public final class PaymentFactory {

	private PaymentFactory() {
	}

	public static List<PaymentInstallment> create(Succession succession, Timeline timeline) {
		int price = succession.getPrice();
		int deposit = Math.round(price * 0.1f);
		int interim = Math.round(price * 0.3f);
		int balance = price - deposit - interim * 2;
		return List.of(
				new PaymentInstallment(succession, 1, "계약금", deposit, timeline.planDue()),
				new PaymentInstallment(succession, 2, "중도금 1차", interim, timeline.stages().get(1).start()),
				new PaymentInstallment(succession, 3, "중도금 2차", interim, timeline.apprenticeEnd()),
				new PaymentInstallment(succession, 4, "잔금", balance, timeline.retireDate()));
	}

	/** 일정이 바뀌면 아직 확인되지 않은 납입의 날짜를 새 일정에 맞춘다 */
	public static void reschedule(List<PaymentInstallment> installments, Timeline timeline) {
		for (PaymentInstallment p : installments) {
			switch (p.getSeq()) {
				case 1 -> p.reschedule(timeline.planDue());
				case 2 -> p.reschedule(timeline.stages().get(1).start());
				case 3 -> p.reschedule(timeline.apprenticeEnd());
				default -> p.reschedule(timeline.retireDate());
			}
		}
	}

}
