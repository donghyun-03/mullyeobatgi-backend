package com.danditon.mullyeobatgi.schedule;

import com.danditon.mullyeobatgi.auth.AuthUser;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

/**
 * ④ 가일정
 */
@RestController
@RequestMapping("/api/successions/{successionId}/schedule")
@RequiredArgsConstructor
public class ScheduleController {

	private final ScheduleService scheduleService;

	public record ChangeRequestBody(@NotNull LocalDate retireDate, @Size(max = 300) String reason) {
	}

	@GetMapping
	public ScheduleResponse view(@AuthenticationPrincipal AuthUser me, @PathVariable Long successionId) {
		return scheduleService.view(successionId, me);
	}

	@PostMapping("/change-requests")
	public ScheduleResponse requestChange(@AuthenticationPrincipal AuthUser me, @PathVariable Long successionId,
			@Valid @RequestBody ChangeRequestBody body) {
		return scheduleService.requestChange(successionId, me, body.retireDate(), body.reason());
	}

	@PostMapping("/change-requests/{requestId}/approve")
	public ScheduleResponse approve(@AuthenticationPrincipal AuthUser me, @PathVariable Long successionId,
			@PathVariable Long requestId) {
		return scheduleService.decide(successionId, requestId, me, true);
	}

	@PostMapping("/change-requests/{requestId}/reject")
	public ScheduleResponse reject(@AuthenticationPrincipal AuthUser me, @PathVariable Long successionId,
			@PathVariable Long requestId) {
		return scheduleService.decide(successionId, requestId, me, false);
	}

}
