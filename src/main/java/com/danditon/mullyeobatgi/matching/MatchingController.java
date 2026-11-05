package com.danditon.mullyeobatgi.matching;

import com.danditon.mullyeobatgi.auth.AuthUser;
import com.danditon.mullyeobatgi.user.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * ② 매칭
 */
@RestController
@RequiredArgsConstructor
public class MatchingController {

	private final MatchingService matchingService;

	@GetMapping("/api/owner/matches")
	public List<YouthCandidateResponse> youthCandidates(@AuthenticationPrincipal AuthUser me) {
		me.requireRole(Role.OWNER);
		return matchingService.candidatesFor(me.id());
	}

	@GetMapping("/api/youth/matches")
	public List<ShopCandidateResponse> shopCandidates(@AuthenticationPrincipal AuthUser me) {
		me.requireRole(Role.YOUTH);
		return matchingService.shopsFor(me.id());
	}

}
