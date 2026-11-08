package com.danditon.mullyeobatgi.succession;

import com.danditon.mullyeobatgi.user.Role;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.List;

/**
 * 홈 화면용 승계 요약과, 지금 내가 해야 할 일 목록.
 */
@Schema(description = "홈 화면용 승계 요약과 지금 내가 해야 할 일")
public record SuccessionSummary(
		@Schema(description = "승계 id", example = "1")
		Long id,
		SuccessionStatus status,
		@Schema(description = "단계 이름", example = "수습")
		String statusLabel,
		@Schema(description = "이 화면을 보는 내가 사장님(OWNER)인지 청년(YOUTH)인지")
		Role mySide,
		Long shopId,
		@Schema(example = "남문 손칼국수")
		String shopName,
		Long ownerId,
		@Schema(example = "김순자")
		String ownerName,
		Long youthId,
		@Schema(example = "이도윤")
		String youthName,
		@Schema(description = "인수 금액 (만 원)", example = "4800")
		int price,
		@Schema(description = "은퇴 희망일 (승계 완료일)", example = "2027-08-31")
		LocalDate retireDate,
		@Schema(description = "은퇴 희망일까지 남은 날", example = "300")
		long daysLeft,
		@Schema(description = "진행률 0~100", example = "45")
		int progressPercent,
		@Schema(description = "체크리스트: 양쪽 확인된 항목 수 / 전체")
		Count checklist,
		@Schema(description = "계획 초안이 있는지")
		boolean planCreated,
		@Schema(description = "두 사람이 계획에 동의했는지")
		boolean planAgreedByBoth,
		@Schema(description = "수습 로드맵: 승인된 일 수 / 전체 (계획 동의 전에는 0/0)")
		Count roadmap,
		@Schema(description = "납입: 사장님이 확인한 금액 / 인수 금액 (만 원)")
		Count payments,
		@Schema(description = "다음 일정. 승계가 끝나면 null")
		NextStep nextStep,
		@Schema(description = "지금 내 차례인 할 일. 사장님과 청년에게 다르게 나와요")
		List<Action> pendingActions) {

	public record Count(int done, int total) {
	}

	public record NextStep(
			@Schema(example = "수습 마무리")
			String title,
			@Schema(example = "2027-06-30")
			LocalDate dueDate) {
	}

	@Schema(description = """
			할 일 종류: SCHEDULE_CHANGE 상대방의 일정 변경 요청, CHECKLIST 체크리스트 확인, PLAN_CREATE 계획 초안 만들기,
			PLAN_AGREE 계획 동의, ROADMAP_APPROVE 수습 단계 승인(사장님), ROADMAP_RETRY 다시 해 보기(청년),
			PAYMENT_CONFIRM 납입 확인(사장님), PAYMENT_DUE 7일 안에 낼 금액(청년)""")
	public enum ActionType {
		CHECKLIST, PLAN_CREATE, PLAN_AGREE, ROADMAP_APPROVE, ROADMAP_RETRY, PAYMENT_CONFIRM, PAYMENT_DUE,
		SCHEDULE_CHANGE
	}

	/** targetId 는 화면에서 바로 처리할 대상(로드맵 일, 납입, 일정 변경 요청)의 id */
	@Schema(description = "할 일 하나")
	public record Action(
			ActionType type,
			@Schema(example = "수습 단계 승인 요청")
			String title,
			@Schema(example = "이도윤 님이 '칼국수 혼자 만들기'를 마쳤다고 해요")
			String description,
			@Schema(description = "바로 처리할 대상 id (로드맵 일, 납입, 일정 변경 요청, 계획). 없으면 null")
			Long targetId) {
	}

}
