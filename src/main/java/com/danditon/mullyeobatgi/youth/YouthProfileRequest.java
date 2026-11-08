package com.danditon.mullyeobatgi.youth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.util.List;

@Schema(description = "청년 등록·수정 요청. 금액은 만 원 단위")
public record YouthProfileRequest(
		@Schema(description = "나이", example = "29")
		@Min(15) @Max(80) Integer age,
		@Schema(description = "음식점 경력. NONE 없음, UNDER_1Y 1년 미만, ONE_TO_3Y 1~3년, OVER_3Y 3년 이상",
				example = "ONE_TO_3Y")
		@NotNull Experience experience,
		@Schema(description = "경력 설명. 매칭 이유에 함께 보여요", example = "한식 주방 2년")
		@Size(max = 100) String experienceDetail,
		@Schema(description = "지금 준비된 자금 (만 원)", example = "3000")
		@PositiveOrZero int fundsReady,
		@Schema(description = "대출 계획이 있는지", example = "true")
		boolean loanPlanned,
		@Schema(description = "대출로 마련할 금액 (만 원). 선택. loanPlanned 가 true 인데 비우면 모자란 금액을 대출로 채운다고 보고 점수를 내요",
				example = "1800")
		@PositiveOrZero int loanAmount,
		@Schema(description = "희망 업종. 가게의 category 와 같은 글자로 비교해요", example = "[\"한식\", \"면 요리\"]")
		@Size(max = 10) List<@Size(max = 20) String> preferredCategories,
		@Schema(description = "희망 지역(구). 가게의 district 와 같은 글자로 비교해요", example = "[\"중구\", \"남구\"]")
		@Size(max = 10) List<@Size(max = 20) String> preferredDistricts,
		@Schema(description = "새벽 근무가 가능한지. 선택. 비우면 '아직 모름'으로 보고 매칭 점수를 절반만 줘요", example = "true")
		Boolean earlyMorningOk,
		@Schema(description = "자기소개", example = "면 요리를 제대로 배우고 싶어요")
		@Size(max = 500) String intro) {
}
