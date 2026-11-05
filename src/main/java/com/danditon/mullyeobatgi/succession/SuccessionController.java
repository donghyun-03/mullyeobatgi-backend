package com.danditon.mullyeobatgi.succession;

import com.danditon.mullyeobatgi.auth.AuthUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 홈 화면의 승계 요약과 확인할 일.
 */
@RestController
@RequestMapping("/api/successions")
@RequiredArgsConstructor
public class SuccessionController {

	private final SuccessionService successionService;

	@GetMapping("/current")
	public SuccessionSummary current(@AuthenticationPrincipal AuthUser me) {
		return successionService.current(me);
	}

	@GetMapping("/{successionId}")
	public SuccessionSummary get(@AuthenticationPrincipal AuthUser me, @PathVariable Long successionId) {
		return successionService.get(successionId, me);
	}

}
