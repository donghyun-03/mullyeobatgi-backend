package com.danditon.mullyeobatgi.youth;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * ① 청년의 경력, 자금, 희망 조건. 금액은 만 원 단위.
 */
public record YouthProfileRequest(
		@Min(15) @Max(80) Integer age,
		@NotNull Experience experience,
		@Size(max = 100) String experienceDetail,
		@PositiveOrZero int fundsReady,
		boolean loanPlanned,
		@PositiveOrZero int loanAmount,
		@Size(max = 10) List<@Size(max = 20) String> preferredCategories,
		@Size(max = 10) List<@Size(max = 20) String> preferredDistricts,
		boolean earlyMorningOk,
		@Size(max = 500) String intro) {
}
