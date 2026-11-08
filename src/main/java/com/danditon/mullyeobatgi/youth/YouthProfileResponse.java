package com.danditon.mullyeobatgi.youth;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "청년 정보. 금액은 만 원 단위. userId 는 사장님이 제안할 때 쓰는 youthId 와 같아요")
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
