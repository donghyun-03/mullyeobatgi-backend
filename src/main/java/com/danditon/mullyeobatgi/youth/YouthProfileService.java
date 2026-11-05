package com.danditon.mullyeobatgi.youth;

import com.danditon.mullyeobatgi.common.ApiException;
import com.danditon.mullyeobatgi.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class YouthProfileService {

	private final YouthProfileRepository profiles;
	private final UserRepository users;
	private final Clock clock;

	@Transactional(readOnly = true)
	public YouthProfileResponse myProfile(Long userId) {
		return YouthProfileResponse.from(getByUser(userId));
	}

	@Transactional
	public YouthProfileResponse save(Long userId, YouthProfileRequest request) {
		LocalDateTime now = LocalDateTime.now(clock);
		YouthProfile profile = profiles.findByUserId(userId).orElse(null);
		if (profile == null) {
			profile = profiles.save(new YouthProfile(users.getReferenceById(userId), request, now));
		} else {
			profile.update(request, now);
		}
		return YouthProfileResponse.from(profile);
	}

	public YouthProfile getByUser(Long userId) {
		return profiles.findByUserId(userId).orElseThrow(() -> ApiException.notFound("먼저 청년 정보를 등록해 주세요"));
	}

}
