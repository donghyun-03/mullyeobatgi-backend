package com.danditon.mullyeobatgi.roadmap;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

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
