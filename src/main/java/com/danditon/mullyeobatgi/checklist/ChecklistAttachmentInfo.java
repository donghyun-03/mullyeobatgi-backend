package com.danditon.mullyeobatgi.checklist;

import com.danditon.mullyeobatgi.user.Role;

import java.time.LocalDateTime;

/**
 * 파일 내용(data)을 빼고 조회한 서류 목록 한 줄.
 */
public record ChecklistAttachmentInfo(Long id, Long itemId, Role uploadedBy, String fileName, String contentType,
		long sizeBytes, LocalDateTime createdAt) {
}
