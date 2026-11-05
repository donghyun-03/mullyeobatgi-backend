package com.danditon.mullyeobatgi.offer;

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
