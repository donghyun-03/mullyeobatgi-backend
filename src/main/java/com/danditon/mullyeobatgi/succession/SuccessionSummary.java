package com.danditon.mullyeobatgi.succession;

import com.danditon.mullyeobatgi.user.Role;

import java.time.LocalDate;
import java.util.List;

/**
 * 홈 화면용 승계 요약과, 지금 내가 해야 할 일 목록.
 */
public record SuccessionSummary(
		Long id,
		SuccessionStatus status,
		String statusLabel,
		Role mySide,
		Long shopId,
		String shopName,
		Long ownerId,
		String ownerName,
		Long youthId,
		String youthName,
		int price,
		LocalDate retireDate,
		long daysLeft,
		int progressPercent,
		Count checklist,
		boolean planCreated,
		boolean planAgreedByBoth,
		Count roadmap,
		Count payments,
		NextStep nextStep,
		List<Action> pendingActions) {

	public record Count(int done, int total) {
	}

	public record NextStep(String title, LocalDate dueDate) {
	}

	public enum ActionType {
		CHECKLIST, PLAN_CREATE, PLAN_AGREE, ROADMAP_APPROVE, ROADMAP_RETRY, PAYMENT_CONFIRM, PAYMENT_DUE,
		SCHEDULE_CHANGE
	}

	/** targetId 는 화면에서 바로 처리할 대상(로드맵 일, 납입, 일정 변경 요청)의 id */
	public record Action(ActionType type, String title, String description, Long targetId) {
	}

}
