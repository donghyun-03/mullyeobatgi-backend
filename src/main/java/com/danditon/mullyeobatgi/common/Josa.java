package com.danditon.mullyeobatgi.common;

/**
 * 앞 글자의 받침에 맞춰 조사를 붙인다. 예: 레시피를, 설비를, 칼국수를, 국밥을.
 */
public final class Josa {

	private Josa() {
	}

	public static String eulReul(String word) {
		return word + (hasFinalConsonant(word) ? "을" : "를");
	}

	public static String iGa(String word) {
		return word + (hasFinalConsonant(word) ? "이" : "가");
	}

	public static String waGwa(String word) {
		return word + (hasFinalConsonant(word) ? "과" : "와");
	}

	public static String ieyo(String word) {
		return word + (hasFinalConsonant(word) ? "이에요" : "예요");
	}

	static boolean hasFinalConsonant(String word) {
		if (word == null || word.isEmpty()) {
			return false;
		}
		char last = word.charAt(word.length() - 1);
		if (last < 0xAC00 || last > 0xD7A3) {
			// 한글이 아니면 받침이 있다고 보고 '을/이'를 쓴다
			return true;
		}
		return (last - 0xAC00) % 28 != 0;
	}

}
