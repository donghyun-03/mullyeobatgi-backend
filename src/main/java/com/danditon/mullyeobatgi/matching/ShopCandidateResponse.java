package com.danditon.mullyeobatgi.matching;

import java.time.LocalDate;
import java.util.List;

/**
 * 청년이 보는 가게 후보. 제안은 사장님이 하므로 청년은 둘러보기만 한다.
 */
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
