package com.danditon.mullyeobatgi.succession;

import java.time.LocalDate;
import java.util.List;

/**
 * ④ 은퇴 희망일에서 거꾸로 계산한 승계 날짜들.
 */
public record Timeline(
		LocalDate acceptedDate,
		LocalDate checklistDue,
		LocalDate planDue,
		LocalDate apprenticeStart,
		LocalDate apprenticeEnd,
		LocalDate handoverStart,
		LocalDate retireDate,
		List<Period> stages,
		List<String> warnings) {

	/** 수습 로드맵 한 단계의 기간 */
	public record Period(LocalDate start, LocalDate end) {
	}

}
