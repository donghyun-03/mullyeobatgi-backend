package com.danditon.mullyeobatgi.matching;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.List;

/**
 * 청년이 보는 가게 후보. 제안은 사장님이 하므로 청년은 둘러보기만 한다.
 */
@Schema(description = "청년이 보는 가게 후보. 금액은 만 원 단위, score 는 100점 만점")
public record ShopCandidateResponse(
		Long shopId,
		String shopName,
		String ownerName,
		String category,
		String mainMenu,
		String district,
		Integer openedYear,
		int askingPrice,
		LocalDate retireDate,
		int score,
		List<MatchScore.Part> breakdown,
		List<MatchScore.Reason> reasons) {
}
