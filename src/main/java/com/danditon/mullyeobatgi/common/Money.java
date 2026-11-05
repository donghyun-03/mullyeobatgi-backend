package com.danditon.mullyeobatgi.common;

import java.text.NumberFormat;
import java.util.Locale;

/**
 * 금액은 모두 만 원 단위 정수로 다룬다.
 */
public final class Money {

	private Money() {
	}

	public static String format(int manwon) {
		return NumberFormat.getNumberInstance(Locale.KOREA).format(manwon) + "만 원";
	}

}
