package com.danditon.mullyeobatgi.user;

/**
 * 로그인한 사용자와, 첫 화면을 고르는 데 필요한 진행 상태.
 */
public record MeResponse(
		Long id,
		String name,
		Role role,
		boolean hasShop,
		boolean hasYouthProfile,
		Long successionId,
		String successionStatus) {
}
