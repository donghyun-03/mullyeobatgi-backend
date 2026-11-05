package com.danditon.mullyeobatgi.shop;

import java.time.LocalDate;

public record ShopResponse(
		Long id,
		Long ownerId,
		String ownerName,
		String name,
		String category,
		String mainMenu,
		String district,
		String address,
		Integer openedYear,
		Integer monthlySales,
		int askingPrice,
		String handoverItems,
		LocalDate retireDate,
		int apprenticeMonths,
		String workHours,
		boolean earlyMorning,
		String message) {

	public static ShopResponse from(Shop shop) {
		return new ShopResponse(shop.getId(), shop.getOwner().getId(), shop.getOwner().getName(), shop.getName(),
				shop.getCategory(), shop.getMainMenu(), shop.getDistrict(), shop.getAddress(), shop.getOpenedYear(),
				shop.getMonthlySales(), shop.getAskingPrice(), shop.getHandoverItems(), shop.getRetireDate(),
				shop.getApprenticeMonths(), shop.getWorkHours(), shop.isEarlyMorning(), shop.getMessage());
	}

}
