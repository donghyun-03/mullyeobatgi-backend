package com.danditon.mullyeobatgi.user;

import com.danditon.mullyeobatgi.auth.AuthUser;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/me")
@RequiredArgsConstructor
public class MeController {

	private final MeService meService;

	public record RoleRequest(@NotNull Role role) {
	}

	@GetMapping
	public MeResponse me(@AuthenticationPrincipal AuthUser me) {
		return meService.me(me.id());
	}

	@PutMapping("/role")
	public MeResponse chooseRole(@AuthenticationPrincipal AuthUser me, @Valid @RequestBody RoleRequest request) {
		return meService.chooseRole(me.id(), request.role());
	}

}
