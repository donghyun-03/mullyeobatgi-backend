package com.danditon.mullyeobatgi.user;

import com.danditon.mullyeobatgi.common.ApiException;
import com.danditon.mullyeobatgi.shop.ShopRepository;
import com.danditon.mullyeobatgi.succession.Succession;
import com.danditon.mullyeobatgi.succession.SuccessionRepository;
import com.danditon.mullyeobatgi.youth.YouthProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MeService {

	private final UserRepository users;
	private final ShopRepository shops;
	private final YouthProfileRepository youthProfiles;
	private final SuccessionRepository successions;

	@Transactional(readOnly = true)
	public MeResponse me(Long userId) {
		User user = users.findById(userId).orElseThrow(() -> ApiException.notFound("사용자를 찾을 수 없어요"));
		Succession succession = successions.findLatestFor(userId).orElse(null);
		return new MeResponse(
				user.getId(),
				user.getName(),
				user.getRole(),
				shops.existsByOwnerId(userId),
				youthProfiles.existsByUserId(userId),
				succession == null ? null : succession.getId(),
				succession == null ? null : succession.getStatus().name());
	}

	/**
	 * 가게나 청년 정보를 등록하기 전까지만 역할을 바꿀 수 있다.
	 */
	@Transactional
	public MeResponse chooseRole(Long userId, Role role) {
		User user = users.findById(userId).orElseThrow(() -> ApiException.notFound("사용자를 찾을 수 없어요"));
		if (user.getRole() != null && user.getRole() != role
				&& (shops.existsByOwnerId(userId) || youthProfiles.existsByUserId(userId))) {
			throw ApiException.conflict("ROLE_LOCKED", "이미 등록한 정보가 있어서 역할을 바꿀 수 없어요");
		}
		user.chooseRole(role);
		return me(userId);
	}

}
