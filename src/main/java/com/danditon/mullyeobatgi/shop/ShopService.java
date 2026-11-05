package com.danditon.mullyeobatgi.shop;

import com.danditon.mullyeobatgi.common.ApiException;
import com.danditon.mullyeobatgi.succession.TimelinePlanner;
import com.danditon.mullyeobatgi.user.User;
import com.danditon.mullyeobatgi.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ShopService {

	private final ShopRepository shops;
	private final UserRepository users;
	private final Clock clock;

	@Transactional(readOnly = true)
	public ShopResponse myShop(Long ownerId) {
		return ShopResponse.from(getByOwner(ownerId));
	}

	@Transactional
	public ShopResponse save(Long ownerId, ShopRequest request) {
		LocalDate earliest = TimelinePlanner.earliestRetireDate(LocalDate.now(clock), request.apprenticeMonthsOrDefault());
		if (request.retireDate().isBefore(earliest)) {
			throw ApiException.badRequest("RETIRE_DATE_TOO_SOON",
					"은퇴 희망일이 너무 가까워요. 수습 기간을 넣으면 " + earliest + " 이후로 정해 주세요");
		}
		LocalDateTime now = LocalDateTime.now(clock);
		Shop shop = shops.findByOwnerId(ownerId).orElse(null);
		if (shop == null) {
			User owner = users.getReferenceById(ownerId);
			shop = shops.save(new Shop(owner, request, now));
		} else {
			shop.update(request, now);
		}
		return ShopResponse.from(shop);
	}

	public Shop getByOwner(Long ownerId) {
		return shops.findByOwnerId(ownerId).orElseThrow(() -> ApiException.notFound("먼저 가게 정보를 등록해 주세요"));
	}

}
