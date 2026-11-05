package com.danditon.mullyeobatgi.common;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * 화면에 그대로 보여 줄 수 있는 한국어 메시지를 담은 예외.
 */
@Getter
public class ApiException extends RuntimeException {

	private final HttpStatus status;
	private final String code;

	public ApiException(HttpStatus status, String code, String message) {
		super(message);
		this.status = status;
		this.code = code;
	}

	public static ApiException notFound(String message) {
		return new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", message);
	}

	public static ApiException forbidden(String message) {
		return new ApiException(HttpStatus.FORBIDDEN, "FORBIDDEN", message);
	}

	public static ApiException badRequest(String code, String message) {
		return new ApiException(HttpStatus.BAD_REQUEST, code, message);
	}

	public static ApiException conflict(String code, String message) {
		return new ApiException(HttpStatus.CONFLICT, code, message);
	}

}
