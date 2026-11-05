package com.danditon.mullyeobatgi.matching;

import com.danditon.mullyeobatgi.offer.OfferStatus;

import java.util.List;

/**
 * 사장님이 보는 청년 후보.
 */
public record YouthCandidateResponse(
		Long youthId,
		String name,
		Integer age,
		String experienceLabel,
		String experienceDetail,
		int fundsReady,
		int score,
		List<MatchScore.Part> breakdown,
		List<MatchScore.Reason> reasons,
		String aiComment,
		Long offerId,
		OfferStatus offerStatus) {
}
