package com.danditon.mullyeobatgi.checklist;

import com.danditon.mullyeobatgi.common.Money;
import com.danditon.mullyeobatgi.succession.Succession;
import com.danditon.mullyeobatgi.user.Role;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * ⑤ 승계 전에 사장님과 청년이 각자 확인하는 항목.
 */
@Entity
@Table(name = "checklist_items")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChecklistItem {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "succession_id")
	private Succession succession;

	@Column(nullable = false)
	private int seq;

	@Column(nullable = false, length = 30)
	private String code;

	@Column(nullable = false, length = 100)
	private String title;

	private String description;

	private LocalDateTime ownerConfirmedAt;

	private String ownerNote;

	private LocalDateTime youthConfirmedAt;

	private String youthNote;

	private ChecklistItem(Succession succession, int seq, String code, String title, String description) {
		this.succession = succession;
		this.seq = seq;
		this.code = code;
		this.title = title;
		this.description = description;
	}

	public static List<ChecklistItem> defaults(Succession s) {
		return List.of(
				new ChecklistItem(s, 1, "LANDLORD", "건물주 동의",
						"임대차 계약을 " + s.getYouth().getName() + " 님 이름으로 넘겨도 되는지 건물주에게 확인해요"),
				new ChecklistItem(s, 2, "DEBT", "채무와 미납금", "가게 대출, 밀린 세금·공과금이 없는지 확인해요"),
				new ChecklistItem(s, 3, "FUNDS", "인수 자금",
						"인수 금액 " + Money.format(s.getPrice()) + "을 어떻게 마련할지 확인해요"),
				new ChecklistItem(s, 4, "LABOR", "수습 근로계약",
						"수습 " + s.getApprenticeMonths() + "개월 동안의 근무 시간과 급여를 정해요"),
				new ChecklistItem(s, 5, "STAFF", "직원 고용 승계", "지금 일하는 직원이 계속 일할지 정해요"),
				new ChecklistItem(s, 6, "LICENSE", "영업 신고 명의 변경", "영업 신고와 사업자 등록을 바꾸는 날짜를 정해요"));
	}

	public void confirm(Role side, String note, LocalDateTime now) {
		if (side == Role.OWNER) {
			ownerConfirmedAt = now;
			ownerNote = note;
		} else {
			youthConfirmedAt = now;
			youthNote = note;
		}
	}

	public void unconfirm(Role side) {
		if (side == Role.OWNER) {
			ownerConfirmedAt = null;
		} else {
			youthConfirmedAt = null;
		}
	}

	public boolean confirmedBy(Role side) {
		return (side == Role.OWNER ? ownerConfirmedAt : youthConfirmedAt) != null;
	}

	public boolean isDone() {
		return ownerConfirmedAt != null && youthConfirmedAt != null;
	}

}
