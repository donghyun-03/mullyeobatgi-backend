package com.danditon.mullyeobatgi.youth;

import com.danditon.mullyeobatgi.auth.AuthUser;
import com.danditon.mullyeobatgi.user.Role;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * ① 청년 등록
 */
@RestController
@RequestMapping("/api/youth/profile")
@RequiredArgsConstructor
public class YouthProfileController {

	private final YouthProfileService youthProfileService;

	@GetMapping
	public YouthProfileResponse myProfile(@AuthenticationPrincipal AuthUser me) {
		me.requireRole(Role.YOUTH);
		return youthProfileService.myProfile(me.id());
	}

	@PutMapping
	public YouthProfileResponse save(@AuthenticationPrincipal AuthUser me,
			@Valid @RequestBody YouthProfileRequest request) {
		me.requireRole(Role.YOUTH);
		return youthProfileService.save(me.id(), request);
	}

}
