package com.danditon.mullyeobatgi.matching;

import java.util.List;

/**
 * ② 적합도 점수(100점)와 그 이유.
 */
public record MatchScore(int total, List<Part> breakdown, List<Reason> reasons) {

	public record Part(String name, int points, int max, String level) {
	}

	public enum ReasonType {
		GOOD, CAUTION
	}

	public record Reason(ReasonType type, String text) {
	}

}
