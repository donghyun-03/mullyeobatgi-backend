package com.danditon.mullyeobatgi.offer;

import com.danditon.mullyeobatgi.shop.Shop;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 제안 화면에 필요한 가게·조건 요약을 함께 담는다.
 */
public record OfferResponse(
		Long id,
		OfferStatus status,
		int price,
		String message,
		int matchScore,
		LocalDateTime expiresAt,
		LocalDateTime createdAt,
		Long shopId,
		String shopName,
		String category,
		String mainMenu,
		String district,
		Integer openedYear,
		String handoverItems,
		LocalDate retireDate,
		int apprenticeMonths,
		String workHours,
		Long ownerId,
		String ownerName,
		Long youthId,
		String youthName,
		Long successionId) {

	public static OfferResponse from(Offer offer, Long successionId) {
		Shop shop = offer.getShop();
		return new OfferResponse(offer.getId(), offer.getStatus(), offer.getPrice(), offer.getMessage(),
				offer.getMatchScore(), offer.getExpiresAt(), offer.getCreatedAt(), shop.getId(), shop.getName(),
				shop.getCategory(), shop.getMainMenu(), shop.getDistrict(), shop.getOpenedYear(),
				shop.getHandoverItems(), shop.getRetireDate(), shop.getApprenticeMonths(), shop.getWorkHours(),
				shop.getOwner().getId(), shop.getOwner().getName(), offer.getYouth().getId(),
				offer.getYouth().getName(), successionId);
	}

}
