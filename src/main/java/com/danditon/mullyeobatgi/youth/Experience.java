package com.danditon.mullyeobatgi.youth;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 음식점 경력 구간. points 는 매칭 점수 중 경력 점수(최대 30점).
 */
@Schema(description = "음식점 경력: NONE 없음(5점), UNDER_1Y 1년 미만(15점), ONE_TO_3Y 1~3년(25점), OVER_3Y 3년 이상(30점)")
public enum Experience {

	NONE("경력 없음", 5),
	UNDER_1Y("경력 1년 미만", 15),
	ONE_TO_3Y("경력 1~3년", 25),
	OVER_3Y("경력 3년 이상", 30);

	private final String label;
	private final int points;

	Experience(String label, int points) {
		this.label = label;
		this.points = points;
	}

	public String label() {
		return label;
	}

	public int points() {
		return points;
	}

}
