package com.danditon.mullyeobatgi.youth;

import java.util.List;

public record YouthProfileResponse(
		Long id,
		Long userId,
		String name,
		Integer age,
		Experience experience,
		String experienceLabel,
		String experienceDetail,
		int fundsReady,
		boolean loanPlanned,
		int loanAmount,
		List<String> preferredCategories,
		List<String> preferredDistricts,
		boolean earlyMorningOk,
		String intro) {

	public static YouthProfileResponse from(YouthProfile p) {
		return new YouthProfileResponse(p.getId(), p.getUser().getId(), p.getUser().getName(), p.getAge(),
				p.getExperience(), p.getExperience().label(), p.getExperienceDetail(), p.getFundsReady(),
				p.isLoanPlanned(), p.getLoanAmount(), p.getPreferredCategories(), p.getPreferredDistricts(),
				p.isEarlyMorningOk(), p.getIntro());
	}

}
