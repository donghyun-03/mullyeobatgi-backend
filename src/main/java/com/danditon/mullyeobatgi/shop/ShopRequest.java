package com.danditon.mullyeobatgi.shop;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record ShopRequest(
		@NotBlank @Size(max = 100) String name,
		@NotBlank @Size(max = 30) String category,
		@Size(max = 50) String mainMenu,
		@NotBlank @Size(max = 20) String district,
		@Size(max = 200) String address,
		@Min(1900) @Max(2100) Integer openedYear,
		@PositiveOrZero Integer monthlySales,
		@Positive int askingPrice,
		@Size(max = 200) String handoverItems,
		@NotNull LocalDate retireDate,
		@Min(3) @Max(12) Integer apprenticeMonths,
		@Size(max = 50) String workHours,
		boolean earlyMorning,
		@Size(max = 500) String message) {

	public static final int DEFAULT_APPRENTICE_MONTHS = 6;

	public int apprenticeMonthsOrDefault() {
		return apprenticeMonths == null ? DEFAULT_APPRENTICE_MONTHS : apprenticeMonths;
	}

}
