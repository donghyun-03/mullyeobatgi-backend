package com.danditon.mullyeobatgi.schedule;

import com.danditon.mullyeobatgi.user.Role;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "승계 가일정. items 의 state 는 DONE 끝남, NOW 지금 할 일, UPCOMING 앞으로 할 일")
public record ScheduleResponse(
		LocalDate retireDate,
		long daysLeft,
		List<Item> items,
		List<String> warnings,
		List<ChangeRequest> changeRequests) {

	public enum State {
		DONE, NOW, UPCOMING
	}

	public record Item(int seq, String code, String title, String note, LocalDate startDate, LocalDate endDate,
			State state) {
	}

	public record ChangeRequest(Long id, Role requestedBy, LocalDate newRetireDate, String reason,
			ScheduleChangeRequest.Status status, LocalDateTime createdAt, boolean mine) {
	}

}
