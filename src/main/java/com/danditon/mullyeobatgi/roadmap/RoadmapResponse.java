package com.danditon.mullyeobatgi.roadmap;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "수습 로드맵. 단계 state 는 DONE, NOW, LOCKED. currentStage 는 지금 단계 번호(모두 끝나면 null)")
public record RoadmapResponse(Integer currentStage, int approvedCount, int taskCount, int waitingApprovalCount,
		List<Stage> stages) {

	public enum StageState {
		DONE, NOW, LOCKED
	}

	public record Stage(Long id, int seq, String title, String description, LocalDate startDate, LocalDate endDate,
			StageState state, int approvedCount, List<Task> tasks) {
	}

	public record Task(Long id, int seq, String title, TaskStatus status, String memo, String feedback,
			LocalDateTime requestedAt, LocalDateTime decidedAt) {
	}

}
