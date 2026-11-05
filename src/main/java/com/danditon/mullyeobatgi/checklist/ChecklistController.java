package com.danditon.mullyeobatgi.checklist;

import com.danditon.mullyeobatgi.auth.AuthUser;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * ⑤ 체크리스트
 */
@RestController
@RequestMapping("/api/successions/{successionId}/checklist")
@RequiredArgsConstructor
public class ChecklistController {

	private final ChecklistService checklistService;

	public record ConfirmRequest(@Size(max = 300) String note) {
	}

	@GetMapping
	public ChecklistResponse view(@AuthenticationPrincipal AuthUser me, @PathVariable Long successionId) {
		return checklistService.view(successionId, me);
	}

	/** 내 쪽 확인. 메모는 선택 */
	@PostMapping("/{itemId}/confirm")
	public ChecklistResponse confirm(@AuthenticationPrincipal AuthUser me, @PathVariable Long successionId,
			@PathVariable Long itemId, @Valid @RequestBody(required = false) ConfirmRequest request) {
		return checklistService.confirm(successionId, itemId, me, request == null ? null : request.note());
	}

	@DeleteMapping("/{itemId}/confirm")
	public ChecklistResponse unconfirm(@AuthenticationPrincipal AuthUser me, @PathVariable Long successionId,
			@PathVariable Long itemId) {
		return checklistService.unconfirm(successionId, itemId, me);
	}

}
