package com.danditon.mullyeobatgi.youth;

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
 * ① 청년 등록
 */
@Tag(name = "① 가게·청년 등록")
@RestController
@RequestMapping("/api/youth/profile")
@RequiredArgsConstructor
public class YouthProfileController {

	private final YouthProfileService youthProfileService;

	@Operation(summary = "내 청년 정보 보기 (청년)",
			description = "등록한 경력, 자금, 희망 조건을 돌려줘요. 아직 등록하지 않았으면 404 예요.")
	@GetMapping
	public YouthProfileResponse myProfile(@Parameter(hidden = true) @AuthenticationPrincipal AuthUser me) {
		me.requireRole(Role.YOUTH);
		return youthProfileService.myProfile(me.id());
	}

	@Operation(summary = "청년 등록·수정 (청년)",
			description = """
					경력, 자금, 희망 조건을 처음이면 만들고, 있으면 고쳐요. 등록해야 사장님의 매칭 목록에 나와요.

					매칭 점수(100점)는 이 값으로 계산해요.
					- **경력 30점**: `NONE` 5, `UNDER_1Y` 15, `ONE_TO_3Y` 25, `OVER_3Y` 30
					- **자금 30점**: (준비된 자금 + 대출 금액)이 인수 금액을 얼마나 채우는지 20점,
					  준비된 자금이 인수 금액의 절반을 넘는지 10점
					- **조건 40점**: 희망 업종 일치 15, 희망 지역 일치 15(지역을 비우면 8), 새벽 근무 조건 10

					`loanPlanned` 가 false 면 `loanAmount` 는 0 으로 저장돼요.

					오류: 403 청년이 아닐 때""")
	@PutMapping
	public YouthProfileResponse save(@Parameter(hidden = true) @AuthenticationPrincipal AuthUser me,
			@Valid @RequestBody YouthProfileRequest request) {
		me.requireRole(Role.YOUTH);
		return youthProfileService.save(me.id(), request);
	}

}
