package com.danditon.mullyeobatgi.roadmap;

import com.danditon.mullyeobatgi.common.ApiException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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

/**
 * ⑦ 청년이 완료를 요청하고 사장님이 승인하는 일 하나.
 */
@Entity
@Table(name = "roadmap_tasks")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RoadmapTask {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "stage_id")
	private RoadmapStage stage;

	@Column(nullable = false)
	private int seq;

	@Column(nullable = false, length = 100)
	private String title;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private TaskStatus status;

	/** 청년이 완료를 요청하며 남긴 말 */
	private String memo;

	/** 사장님이 다시 해 보라고 할 때 남긴 이유 */
	private String feedback;

	private LocalDateTime requestedAt;

	private LocalDateTime decidedAt;

	RoadmapTask(RoadmapStage stage, int seq, String title) {
		this.stage = stage;
		this.seq = seq;
		this.title = title;
		this.status = TaskStatus.TODO;
	}

	public void requestComplete(String memo, LocalDateTime now) {
		if (status != TaskStatus.TODO) {
			throw ApiException.conflict("TASK_NOT_TODO", "이미 완료를 요청했거나 승인된 일이에요");
		}
		this.status = TaskStatus.REQUESTED;
		this.memo = memo;
		this.feedback = null;
		this.requestedAt = now;
	}

	public void approve(LocalDateTime now) {
		requireRequested();
		this.status = TaskStatus.APPROVED;
		this.decidedAt = now;
	}

	public void sendBack(String feedback, LocalDateTime now) {
		requireRequested();
		this.status = TaskStatus.TODO;
		this.feedback = feedback;
		this.decidedAt = now;
	}

	private void requireRequested() {
		if (status != TaskStatus.REQUESTED) {
			throw ApiException.conflict("TASK_NOT_REQUESTED", "완료 요청이 온 일만 승인할 수 있어요");
		}
	}

}
