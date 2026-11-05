package com.danditon.mullyeobatgi.schedule;

import com.danditon.mullyeobatgi.user.Role;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

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
