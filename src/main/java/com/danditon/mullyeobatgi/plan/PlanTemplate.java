package com.danditon.mullyeobatgi.plan;

import com.danditon.mullyeobatgi.common.Josa;
import com.danditon.mullyeobatgi.common.Money;
import com.danditon.mullyeobatgi.payment.PaymentFactory;
import com.danditon.mullyeobatgi.payment.PaymentInstallment;
import com.danditon.mullyeobatgi.succession.Succession;
import com.danditon.mullyeobatgi.succession.Timeline;

import java.time.LocalDate;
import java.util.List;

/**
 * ⑥ 승계 계획 기준 초안. AI 가 없으면 이 초안을 그대로 쓰고, AI 가 있으면 이 초안을 다듬게 한다.
 */
public final class PlanTemplate {

	private PlanTemplate() {
	}

	public static List<PlanDraftSection> build(Succession s, Timeline t) {
		String owner = s.getOwner().getName() + " 사장님";
		String youth = s.getYouth().getName() + " 님";
		String items = s.getShop().getHandoverItems() == null || s.getShop().getHandoverItems().isBlank()
				? "가게 운영에 필요한 것" : s.getShop().getHandoverItems();
		String menu = s.getShop().getMainMenu() == null || s.getShop().getMainMenu().isBlank()
				? "대표 메뉴" : s.getShop().getMainMenu();
		String hours = s.getShop().getWorkHours() == null || s.getShop().getWorkHours().isBlank()
				? "" : " 근무 시간은 " + s.getShop().getWorkHours() + "이에요.";
		List<PaymentInstallment> pay = PaymentFactory.create(s, t);

		return List.of(
				new PlanDraftSection("ACQUISITION", "인수 조건",
						Josa.eulReul(items) + " " + Money.format(s.getPrice()) + "에 넘겨받아요. 계약금 "
								+ Money.format(pay.get(0).getAmount()) + "은 " + date(t.planDue()) + "까지, 중도금 "
								+ Money.format(pay.get(1).getAmount()) + "씩 두 번과 잔금 "
								+ Money.format(pay.get(3).getAmount()) + "은 일정에 맞춰 나눠 내요."),
				new PlanDraftSection("APPRENTICE", "수습 계획",
						date(t.apprenticeStart()) + "부터 " + s.getApprenticeMonths() + "개월 동안 3단계로 배워요." + hours
								+ " 단계를 마치면 " + youth + "이 완료를 요청하고 " + owner + "이 승인해요."),
				new PlanDraftSection("HANDOVER", "운영권 넘기기",
						date(t.handoverStart()) + "부터 " + youth + "이 가게를 운영하고, " + owner + "은 "
								+ date(t.retireDate()) + "까지 곁에서 도와요. " + menu
								+ " 레시피와 단골 손님도 이때까지 함께 넘겨요. 잔금을 내면 승계가 끝나요."),
				new PlanDraftSection("EXIT", "그만두게 되면",
						"수습 중 한쪽이 그만두면 계약금을 뺀 납입 금액을 2주 안에 돌려줘요. 자세한 조건은 실제 계약서에서 정해요."));
	}

	private static String date(LocalDate d) {
		return d.getYear() + "년 " + d.getMonthValue() + "월 " + d.getDayOfMonth() + "일";
	}

}
