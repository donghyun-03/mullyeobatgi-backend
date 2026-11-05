package com.danditon.mullyeobatgi.shop;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

@Schema(description = "가게 등록·수정 요청. 금액은 만 원 단위")
public record ShopRequest(
		@Schema(description = "가게 이름", example = "남문 손칼국수")
		@NotBlank @Size(max = 100) String name,
		@Schema(description = "업종. 청년의 희망 업종과 같은 글자로 비교해요", example = "한식")
		@NotBlank @Size(max = 30) String category,
		@Schema(description = "대표 메뉴. 수습 로드맵과 승계 계획에 들어가요", example = "칼국수")
		@Size(max = 50) String mainMenu,
		@Schema(description = "구. 청년의 희망 지역과 같은 글자로 비교해요", example = "중구")
		@NotBlank @Size(max = 20) String district,
		@Schema(description = "주소", example = "대구 중구 남산동")
		@Size(max = 200) String address,
		@Schema(description = "개업 연도", example = "1987")
		@Min(1900) @Max(2100) Integer openedYear,
		@Schema(description = "월평균 매출 (만 원)", example = "2100")
		@PositiveOrZero Integer monthlySales,
		@Schema(description = "인수 금액 (만 원). 권리금, 설비 등 넘겨주는 것 전체 값", example = "4800")
		@Positive int askingPrice,
		@Schema(description = "인수 금액에 포함되는 것", example = "권리금 · 주방 설비 · 레시피")
		@Size(max = 200) String handoverItems,
		@Schema(description = "은퇴 희망일. 이 날짜에서 거꾸로 승계 일정을 계산해요", example = "2027-08-31")
		@NotNull LocalDate retireDate,
		@Schema(description = "수습 기간 (개월, 3~12). 비우면 6개월", example = "6")
		@Min(3) @Max(12) Integer apprenticeMonths,
		@Schema(description = "근무 시간", example = "새벽 5시~오후 3시")
		@Size(max = 50) String workHours,
		@Schema(description = "새벽 근무가 필요한 가게인지. 청년의 새벽 근무 가능 여부와 비교해요", example = "true")
		boolean earlyMorning,
		@Schema(description = "청년에게 남기는 말. 제안 화면에 보여요", example = "국물 맛만 지켜 주면 다 넘겨줄게요.")
		@Size(max = 500) String message) {

	public static final int DEFAULT_APPRENTICE_MONTHS = 6;

	public int apprenticeMonthsOrDefault() {
		return apprenticeMonths == null ? DEFAULT_APPRENTICE_MONTHS : apprenticeMonths;
	}

}
