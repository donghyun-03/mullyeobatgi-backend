package com.danditon.mullyeobatgi.succession;

import com.danditon.mullyeobatgi.auth.AuthUser;
import com.danditon.mullyeobatgi.common.ApiException;
import com.danditon.mullyeobatgi.user.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 승계에 참여한 사장님과 청년만 그 승계를 볼 수 있다.
 */
@Component
@RequiredArgsConstructor
public class SuccessionAccess {

	private final SuccessionRepository successions;

	public Succession get(Long successionId, AuthUser me) {
		Succession succession = successions.findById(successionId)
				.orElseThrow(() -> ApiException.notFound("승계를 찾을 수 없어요"));
		if (succession.sideOf(me.id()) == null) {
			throw ApiException.forbidden("내가 참여한 승계가 아니에요");
		}
		return succession;
	}

	public Role side(Succession succession, AuthUser me) {
		return succession.sideOf(me.id());
	}

	public Role requireSide(Succession succession, AuthUser me, Role required) {
		Role side = succession.sideOf(me.id());
		if (side != required) {
			throw ApiException.forbidden(required.label() + "만 할 수 있어요");
		}
		return side;
	}

}
