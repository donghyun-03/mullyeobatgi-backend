package com.danditon.mullyeobatgi.common;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.Arrays;
import java.util.List;

/**
 * 희망 업종·지역처럼 짧은 목록을 쉼표로 이어 한 칸에 저장한다.
 */
@Converter
public class StringListConverter implements AttributeConverter<List<String>, String> {

	@Override
	public String convertToDatabaseColumn(List<String> values) {
		if (values == null || values.isEmpty()) {
			return null;
		}
		return String.join(",", values);
	}

	@Override
	public List<String> convertToEntityAttribute(String column) {
		if (column == null || column.isBlank()) {
			return List.of();
		}
		return Arrays.stream(column.split(",")).map(String::trim).filter(s -> !s.isEmpty()).toList();
	}

}
