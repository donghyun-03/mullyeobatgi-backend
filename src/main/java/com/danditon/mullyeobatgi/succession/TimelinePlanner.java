package com.danditon.mullyeobatgi.succession;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * ④ 가일정 역산. 은퇴 희망일에서 거꾸로:
 * 운영권 넘기기 2개월 → 수습 N개월(3단계) → 계획 동의·계약금(수습 12일 전) → 체크리스트(계획 5일 전).
 */
public final class TimelinePlanner {

	public static final int HANDOVER_MONTHS = 2;
	public static final int STAGE_COUNT = 3;
	static final int PLAN_DAYS_BEFORE_APPRENTICE = 12;
	static final int CHECKLIST_DAYS_BEFORE_PLAN = 5;
	static final int MIN_CHECKLIST_DAYS = 7;

	private TimelinePlanner() {
	}

	public static Timeline plan(LocalDate accepted, LocalDate retireDate, int apprenticeMonths) {
		// 월말 은퇴일에서도 1일부터 시작하도록 다음 날 기준으로 개월을 뺀다
		LocalDate handoverStart = retireDate.plusDays(1).minusMonths(HANDOVER_MONTHS);
		LocalDate apprenticeEnd = handoverStart.minusDays(1);
		LocalDate apprenticeStart = handoverStart.minusMonths(apprenticeMonths);
		LocalDate planDue = apprenticeStart.minusDays(PLAN_DAYS_BEFORE_APPRENTICE);
		LocalDate checklistDue = planDue.minusDays(CHECKLIST_DAYS_BEFORE_PLAN);

		List<String> warnings = new ArrayList<>();
		if (checklistDue.isBefore(accepted.plusDays(MIN_CHECKLIST_DAYS))) {
			warnings.add("체크리스트를 확인할 시간이 일주일도 안 돼요. 은퇴 희망일을 늦추는 걸 검토해 보세요");
		}
		if (!apprenticeStart.isAfter(accepted)) {
			warnings.add("수습을 시작해야 할 날이 이미 지났어요. 일정을 다시 정해 주세요");
		}
		return new Timeline(accepted, checklistDue, planDue, apprenticeStart, apprenticeEnd, handoverStart,
				retireDate, stages(apprenticeStart, apprenticeMonths), warnings);
	}

	/**
	 * 수습 기간을 개월 단위로 3단계에 나눈다. 6개월이면 2·2·2, 5개월이면 2·2·1.
	 */
	static List<Timeline.Period> stages(LocalDate apprenticeStart, int apprenticeMonths) {
		List<Timeline.Period> periods = new ArrayList<>();
		LocalDate start = apprenticeStart;
		for (int i = 0; i < STAGE_COUNT; i++) {
			int months = apprenticeMonths / STAGE_COUNT + (i < apprenticeMonths % STAGE_COUNT ? 1 : 0);
			LocalDate next = start.plusMonths(months);
			periods.add(new Timeline.Period(start, next.minusDays(1)));
			start = next;
		}
		return periods;
	}

	/**
	 * 체크리스트에 최소 일주일을 주려면 은퇴 희망일이 이 날짜 이후여야 한다.
	 */
	public static LocalDate earliestRetireDate(LocalDate today, int apprenticeMonths) {
		return today.plusMonths(apprenticeMonths + HANDOVER_MONTHS)
				.plusDays(PLAN_DAYS_BEFORE_APPRENTICE + CHECKLIST_DAYS_BEFORE_PLAN + MIN_CHECKLIST_DAYS);
	}

}
