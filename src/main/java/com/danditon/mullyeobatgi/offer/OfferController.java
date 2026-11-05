package com.danditon.mullyeobatgi.offer;

import com.danditon.mullyeobatgi.auth.AuthUser;
import com.danditon.mullyeobatgi.user.Role;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "③ 제안·수락")
@RestController
@RequestMapping("/api/offers")
@RequiredArgsConstructor
public class OfferController {

	private final OfferService offerService;

	@Schema(description = "승계 제안 요청")
	public record ProposeRequest(
			@Schema(description = "제안할 청년의 사용자 id (매칭 목록의 youthId)", example = "2")
			@NotNull Long youthId,
			@Schema(description = "청년에게 남기는 말", example = "같이 가게를 지켜 주면 좋겠어요")
			@Size(max = 500) String message) {
	}

	@Operation(summary = "승계 제안 보내기 (사장님)",
			description = """
					청년에게 내 가게의 승계를 제안해요. 인수 금액은 지금 가게 정보의 `askingPrice` 로 정해져요.
					청년은 **14일** 안에 답해야 하고, 지나면 `EXPIRED` 가 돼요.

					오류 (409):
					- `SHOP_IN_SUCCESSION` 내 가게가 이미 승계 중
					- `YOUTH_IN_SUCCESSION` 그 청년이 다른 가게와 승계 중
					- `OFFER_EXISTS` 같은 청년에게 보낸 제안이 아직 답을 기다리는 중""")
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public OfferResponse propose(@Parameter(hidden = true) @AuthenticationPrincipal AuthUser me,
			@Valid @RequestBody ProposeRequest request) {
		me.requireRole(Role.OWNER);
		return offerService.propose(me.id(), request.youthId(), request.message());
	}

	@Operation(summary = "내 제안 목록",
			description = """
					사장님은 **보낸 제안**, 청년은 **받은 제안**을 최신순으로 돌려줘요.
					기한이 지난 제안은 이때 `EXPIRED` 로 바뀌어요.
					수락된 제안에는 `successionId` 가 들어 있어요.""")
	@GetMapping
	public List<OfferResponse> myOffers(@Parameter(hidden = true) @AuthenticationPrincipal AuthUser me) {
		return offerService.myOffers(me);
	}

	@Operation(summary = "제안 하나 보기",
			description = "청년의 '받은 제안' 화면에 필요한 가게 정보, 인수 금액, 은퇴 희망일, 수습 조건, 사장님이 남긴 말을 함께 돌려줘요. "
					+ "제안을 보낸 사장님과 받은 청년만 볼 수 있어요 (그 외 403).")
	@GetMapping("/{offerId}")
	public OfferResponse get(@Parameter(hidden = true) @AuthenticationPrincipal AuthUser me,
			@Parameter(description = "제안 id") @PathVariable Long offerId) {
		return offerService.get(me, offerId);
	}

	@Operation(summary = "제안 수락 (청년)",
			description = """
					제안을 수락하면 **승계가 시작**돼요.
					- 승계(`CHECKLIST` 단계), ④ 가일정, ⑤ 체크리스트 6개가 함께 만들어져요.
					- 응답의 `successionId` 로 이후 승계 API를 불러요.
					- 같은 가게에 온 다른 제안과, 이 청년이 받은 다른 제안은 `CLOSED` 로 마감돼요.

					오류 (409): `OFFER_EXPIRED` 기한 지남, `OFFER_NOT_PENDING` 이미 답함, `ALREADY_IN_SUCCESSION` 진행 중인 승계 있음""")
	@PostMapping("/{offerId}/accept")
	public OfferResponse accept(@Parameter(hidden = true) @AuthenticationPrincipal AuthUser me,
			@Parameter(description = "제안 id") @PathVariable Long offerId) {
		return offerService.accept(me, offerId);
	}

	@Operation(summary = "제안 거절 (청년)",
			description = "제안을 거절해요. 답을 기다리는 제안(`PROPOSED`)만 거절할 수 있어요 (아니면 409 `OFFER_NOT_PENDING`).")
	@PostMapping("/{offerId}/reject")
	public OfferResponse reject(@Parameter(hidden = true) @AuthenticationPrincipal AuthUser me,
			@Parameter(description = "제안 id") @PathVariable Long offerId) {
		return offerService.reject(me, offerId);
	}

	@Operation(summary = "제안 거둬들이기 (사장님)",
			description = "청년이 답하기 전에 보낸 제안을 취소해요. 답을 기다리는 제안만 취소할 수 있어요 (아니면 409 `OFFER_NOT_PENDING`).")
	@PostMapping("/{offerId}/cancel")
	public OfferResponse cancel(@Parameter(hidden = true) @AuthenticationPrincipal AuthUser me,
			@Parameter(description = "제안 id") @PathVariable Long offerId) {
		return offerService.cancel(me, offerId);
	}

}
