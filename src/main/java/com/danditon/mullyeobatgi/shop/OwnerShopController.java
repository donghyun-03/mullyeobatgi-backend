package com.danditon.mullyeobatgi.shop;

import com.danditon.mullyeobatgi.auth.AuthUser;
import com.danditon.mullyeobatgi.user.Role;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * ① 사장님 가게 등록
 */
@Tag(name = "① 가게·청년 등록")
@RestController
@RequestMapping("/api/owner/shop")
@RequiredArgsConstructor
public class OwnerShopController {

	private final ShopService shopService;

	@Operation(summary = "내 가게 보기 (사장님)",
			description = "등록한 가게 정보를 돌려줘요. 아직 등록하지 않았으면 404 예요.")
	@GetMapping
	public ShopResponse myShop(@Parameter(hidden = true) @AuthenticationPrincipal AuthUser me) {
		me.requireRole(Role.OWNER);
		return shopService.myShop(me.id());
	}

	@Operation(summary = "가게 등록·수정 (사장님)",
			description = """
					가게 정보를 처음이면 만들고, 있으면 고쳐요. 등록 화면의 여러 단계를 모아 한 번에 보내면 돼요.

					- **은퇴 희망일**(`retireDate`)에서 거꾸로 승계 가일정을 계산해요.
					  수습 기간 + 운영권 넘기기 2개월 + 계획·체크리스트 기간이 들어가야 해서,
					  너무 가까우면 400 `RETIRE_DATE_TOO_SOON` 과 함께 가장 빠른 날짜를 알려 줘요.
					  (수습 6개월이면 오늘로부터 약 8개월 24일 이후)
					- **인수 금액**(`askingPrice`)은 만 원 단위예요. 청년의 자금과 비교해 매칭 점수를 내요.
					- **업종**(`category`)과 **지역**(`district`)은 청년의 희망 업종·지역과 같은 글자로 비교해요.
					  예: 한식, 분식, 치킨, 카페 / 중구, 남구, 수성구
					- 이미 승계를 시작했다면, 진행 중인 승계의 금액과 일정은 수락했을 때 값 그대로예요.

					오류: 403 사장님이 아닐 때""")
	@PutMapping
	public ShopResponse save(@Parameter(hidden = true) @AuthenticationPrincipal AuthUser me,
			@Valid @RequestBody ShopRequest request) {
		me.requireRole(Role.OWNER);
		return shopService.save(me.id(), request);
	}

}
