package com.danditon.mullyeobatgi.succession;

/**
 * 승계가 지나가는 단계. 두 사람이 각자 확인해야 다음 단계로 넘어간다.
 */
public enum SuccessionStatus {

	/** ⑤ 체크리스트를 양쪽이 확인하는 중 */
	CHECKLIST("체크리스트 확인"),
	/** ⑥ AI 승계 계획을 고치고 동의하는 중 */
	PLANNING("승계 계획 동의"),
	/** ⑦ 수습 로드맵 진행 중 */
	APPRENTICE("수습"),
	/** 수습을 마치고 운영권을 넘겨받는 중 */
	HANDOVER("운영권 넘겨받기"),
	COMPLETED("승계 완료");

	private final String label;

	SuccessionStatus(String label) {
		this.label = label;
	}

	public String label() {
		return label;
	}

	public boolean isAfter(SuccessionStatus other) {
		return ordinal() > other.ordinal();
	}

}
