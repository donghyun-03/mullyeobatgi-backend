package com.danditon.mullyeobatgi.user;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "역할: OWNER 가게를 물려주는 사장님, YOUTH 가게를 이어받는 청년")
public enum Role {

	/** 가게를 물려주는 사장님 */
	OWNER("사장님"),
	/** 가게를 이어받는 청년 */
	YOUTH("청년");

	private final String label;

	Role(String label) {
		this.label = label;
	}

	public String label() {
		return label;
	}

}
