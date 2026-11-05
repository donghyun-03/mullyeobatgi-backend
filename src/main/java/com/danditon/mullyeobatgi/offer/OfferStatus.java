package com.danditon.mullyeobatgi.offer;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = """
		제안 상태: PROPOSED 답을 기다리는 중, ACCEPTED 수락, REJECTED 거절, CANCELED 사장님이 거둬들임,
		EXPIRED 답할 기한(14일) 지남, CLOSED 가게나 청년이 다른 승계를 시작해서 마감""")
public enum OfferStatus {
	/** 청년의 답을 기다리는 중 */
	PROPOSED,
	ACCEPTED,
	REJECTED,
	/** 사장님이 거둬들임 */
	CANCELED,
	/** 답할 기한이 지남 */
	EXPIRED,
	/** 가게나 청년이 다른 승계를 시작해서 마감됨 */
	CLOSED
}
