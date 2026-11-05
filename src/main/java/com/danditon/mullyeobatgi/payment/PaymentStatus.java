package com.danditon.mullyeobatgi.payment;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "납입 상태: SCHEDULED 아직 안 냄, REPORTED 청년이 냈다고 기록함(사장님 확인 대기), CONFIRMED 사장님이 받았다고 확인함")
public enum PaymentStatus {
	/** 아직 낼 날이 안 됐거나 안 낸 금액 */
	SCHEDULED,
	/** 청년이 냈다고 기록해서 사장님 확인을 기다리는 중 */
	REPORTED,
	/** 사장님이 받았다고 확인함 */
	CONFIRMED
}
