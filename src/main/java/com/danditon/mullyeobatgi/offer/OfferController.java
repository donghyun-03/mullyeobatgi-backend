package com.danditon.mullyeobatgi.offer;

import com.danditon.mullyeobatgi.auth.AuthUser;
import com.danditon.mullyeobatgi.user.Role;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * ③ 제안·수락
 */
@RestController
@RequestMapping("/api/offers")
@RequiredArgsConstructor
public class OfferController {

	private final OfferService offerService;

	public record ProposeRequest(@NotNull Long youthId, @Size(max = 500) String message) {
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public OfferResponse propose(@AuthenticationPrincipal AuthUser me, @Valid @RequestBody ProposeRequest request) {
		me.requireRole(Role.OWNER);
		return offerService.propose(me.id(), request.youthId(), request.message());
	}

	/** 사장님은 보낸 제안, 청년은 받은 제안 */
	@GetMapping
	public List<OfferResponse> myOffers(@AuthenticationPrincipal AuthUser me) {
		return offerService.myOffers(me);
	}

	@GetMapping("/{offerId}")
	public OfferResponse get(@AuthenticationPrincipal AuthUser me, @PathVariable Long offerId) {
		return offerService.get(me, offerId);
	}

	@PostMapping("/{offerId}/accept")
	public OfferResponse accept(@AuthenticationPrincipal AuthUser me, @PathVariable Long offerId) {
		return offerService.accept(me, offerId);
	}

	@PostMapping("/{offerId}/reject")
	public OfferResponse reject(@AuthenticationPrincipal AuthUser me, @PathVariable Long offerId) {
		return offerService.reject(me, offerId);
	}

	@PostMapping("/{offerId}/cancel")
	public OfferResponse cancel(@AuthenticationPrincipal AuthUser me, @PathVariable Long offerId) {
		return offerService.cancel(me, offerId);
	}

}
