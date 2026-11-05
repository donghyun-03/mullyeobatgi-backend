package com.danditon.mullyeobatgi.user;

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
