package com.danditon.mullyeobatgi.plan;

import com.danditon.mullyeobatgi.auth.AuthUser;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * ⑥ AI 승계 계획
 */
@RestController
@RequestMapping("/api/successions/{successionId}/plan")
@RequiredArgsConstructor
public class PlanController {

	private final PlanService planService;

	public record EditRequest(@NotBlank @Size(max = 2000) String content) {
	}

	@GetMapping
	public PlanResponse view(@AuthenticationPrincipal AuthUser me, @PathVariable Long successionId) {
		return planService.view(successionId, me);
	}

	@PostMapping
	public PlanResponse generate(@AuthenticationPrincipal AuthUser me, @PathVariable Long successionId) {
		return planService.generate(successionId, me);
	}

	@PatchMapping("/sections/{sectionId}")
	public PlanResponse edit(@AuthenticationPrincipal AuthUser me, @PathVariable Long successionId,
			@PathVariable Long sectionId, @Valid @RequestBody EditRequest request) {
		return planService.editSection(successionId, sectionId, me, request.content());
	}

	@PostMapping("/agree")
	public PlanResponse agree(@AuthenticationPrincipal AuthUser me, @PathVariable Long successionId) {
		return planService.agree(successionId, me);
	}

}
