package com.danditon.mullyeobatgi.schedule;

import com.danditon.mullyeobatgi.succession.Succession;
import com.danditon.mullyeobatgi.succession.Timeline;
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

import java.time.LocalDate;
import java.util.List;

/**
 * ④ 가일정의 한 줄.
 */
@Entity
@Table(name = "schedule_items")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ScheduleItem {

	public static final String ACCEPT = "ACCEPT";
	public static final String CHECKLIST = "CHECKLIST";
	public static final String PLAN = "PLAN";
	public static final String APPRENTICE = "APPRENTICE";
	public static final String HANDOVER = "HANDOVER";
	public static final String COMPLETE = "COMPLETE";

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

	private String note;

	private LocalDate startDate;

	@Column(nullable = false)
	private LocalDate endDate;

	private ScheduleItem(Succession succession, int seq, String code, String title, String note, LocalDate startDate,
			LocalDate endDate) {
		this.succession = succession;
		this.seq = seq;
		this.code = code;
		this.title = title;
		this.note = note;
		this.startDate = startDate;
		this.endDate = endDate;
	}

	public static List<ScheduleItem> fromTimeline(Succession succession, Timeline t) {
		return List.of(
				new ScheduleItem(succession, 1, ACCEPT, "제안 수락", null, null, t.acceptedDate()),
				new ScheduleItem(succession, 2, CHECKLIST, "체크리스트 확인", "건물주 동의, 채무, 자금, 근로계약", null,
						t.checklistDue()),
				new ScheduleItem(succession, 3, PLAN, "AI 승계 계획 동의 · 계약금", "인수 금액의 10%", null, t.planDue()),
				new ScheduleItem(succession, 4, APPRENTICE, "수습 " + succession.getApprenticeMonths() + "개월",
						"3단계 로드맵, 단계마다 사장님 승인", t.apprenticeStart(), t.apprenticeEnd()),
				new ScheduleItem(succession, 5, HANDOVER, "운영권 넘겨받기", "사장님은 곁에서 도와요", t.handoverStart(),
						t.retireDate().minusDays(1)),
				new ScheduleItem(succession, 6, COMPLETE, "잔금 납입 · 승계 완료", null, null, t.retireDate()));
	}

}
