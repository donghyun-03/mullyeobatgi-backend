package com.danditon.mullyeobatgi.user;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "로그인한 사용자와, 첫 화면을 고르는 데 필요한 진행 상태")
public record MeResponse(
		@Schema(description = "사용자 id", example = "1")
		Long id,
		@Schema(description = "이름", example = "김순자")
		String name,
		@Schema(description = "역할. 아직 고르지 않았으면 null")
		Role role,
		@Schema(description = "가게 정보를 등록했는지 (사장님)")
		boolean hasShop,
		@Schema(description = "청년 정보를 등록했는지 (청년)")
		boolean hasYouthProfile,
		@Schema(description = "가장 최근 승계 id. 승계를 시작한 적이 없으면 null", example = "1")
		Long successionId,
		@Schema(description = "그 승계의 단계: CHECKLIST, PLANNING, APPRENTICE, HANDOVER, COMPLETED", example = "CHECKLIST")
		String successionStatus) {
}
