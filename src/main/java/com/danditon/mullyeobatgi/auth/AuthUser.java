package com.danditon.mullyeobatgi.auth;

import com.danditon.mullyeobatgi.common.ApiException;
import com.danditon.mullyeobatgi.user.Role;

/**
 * 요청마다 토큰에서 꺼낸 로그인 사용자. 컨트롤러에서 {@code @AuthenticationPrincipal} 로 받는다.
 */
public record AuthUser(Long id, String name, Role role) {

	public void requireRole(Role required) {
		if (role == null) {
			throw ApiException.forbidden("먼저 사장님인지 청년인지 골라 주세요");
		}
		if (role != required) {
			throw ApiException.forbidden(required.label() + "만 할 수 있어요");
		}
	}

}
