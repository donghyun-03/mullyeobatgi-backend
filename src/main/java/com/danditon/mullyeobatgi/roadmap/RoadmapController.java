package com.danditon.mullyeobatgi.roadmap;

import com.danditon.mullyeobatgi.auth.AuthUser;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * ⑦ 수습 로드맵
 */
@RestController
@RequestMapping("/api/successions/{successionId}/roadmap")
@RequiredArgsConstructor
public class RoadmapController {

	private final RoadmapService roadmapService;

	public record RequestBodyMemo(@Size(max = 300) String memo) {
	}

	public record SendBackRequest(@NotBlank @Size(max = 300) String reason) {
	}

	@GetMapping
	public RoadmapResponse view(@AuthenticationPrincipal AuthUser me, @PathVariable Long successionId) {
		return roadmapService.view(successionId, me);
	}

	/** 청년: 완료 요청 */
	@PostMapping("/tasks/{taskId}/request")
	public RoadmapResponse requestComplete(@AuthenticationPrincipal AuthUser me, @PathVariable Long successionId,
			@PathVariable Long taskId, @Valid @RequestBody(required = false) RequestBodyMemo body) {
		return roadmapService.requestComplete(successionId, taskId, me, body == null ? null : body.memo());
	}

	/** 사장님: 승인 */
	@PostMapping("/tasks/{taskId}/approve")
	public RoadmapResponse approve(@AuthenticationPrincipal AuthUser me, @PathVariable Long successionId,
			@PathVariable Long taskId) {
		return roadmapService.approve(successionId, taskId, me);
	}

	/** 사장님: 다시 해 보기 */
	@PostMapping("/tasks/{taskId}/send-back")
	public RoadmapResponse sendBack(@AuthenticationPrincipal AuthUser me, @PathVariable Long successionId,
			@PathVariable Long taskId, @Valid @RequestBody SendBackRequest body) {
		return roadmapService.sendBack(successionId, taskId, me, body.reason());
	}

}
