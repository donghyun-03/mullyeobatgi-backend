package com.danditon.mullyeobatgi.roadmap;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "수습 일 상태: TODO 할 일(다시 해 보기 포함), REQUESTED 사장님 승인 기다리는 중, APPROVED 승인됨")
public enum TaskStatus {
	/** 아직 안 했거나, 사장님이 다시 해 보라고 돌려보낸 일 */
	TODO,
	/** 청년이 완료를 요청해서 사장님 승인을 기다리는 중 */
	REQUESTED,
	APPROVED
}
