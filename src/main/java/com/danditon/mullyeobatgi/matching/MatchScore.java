package com.danditon.mullyeobatgi.matching;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * ② 적합도 점수(100점)와 그 이유.
 */
public record MatchScore(int total, List<Part> breakdown, List<Reason> reasons) {

	@Schema(description = "항목별 점수")
	public record Part(
			@Schema(description = "항목: 경력, 자금, 조건", example = "경력")
			String name,
			@Schema(description = "받은 점수", example = "25")
			int points,
			@Schema(description = "만점 (경력 30, 자금 30, 조건 40)", example = "30")
			int max,
			@Schema(description = "높음(80% 이상), 보통(50% 이상), 낮음", example = "높음")
			String level) {
	}

	public enum ReasonType {
		GOOD, CAUTION
	}

	@Schema(description = "매칭 이유 한 줄")
	public record Reason(
			@Schema(description = "GOOD 잘 맞는 점, CAUTION 확인할 점")
			ReasonType type,
			@Schema(description = "화면에 그대로 보여 줄 문장", example = "희망 업종(한식)과 맞아요")
			String text) {
	}

}
