package com.danditon.mullyeobatgi.checklist;

import com.danditon.mullyeobatgi.user.Role;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "체크리스트. doneCount 는 양쪽 모두 확인한 항목 수, completed 는 모든 항목을 양쪽이 확인했는지")
public record ChecklistResponse(
		Role mySide,
		@Schema(description = "체크리스트 마감일 (가일정 기준)", example = "2026-12-15")
		LocalDate dueDate,
		int doneCount,
		int total,
		boolean completed,
		List<Item> items) {

	public record Item(
			Long id,
			int seq,
			String code,
			String title,
			String description,
			boolean ownerConfirmed,
			LocalDateTime ownerConfirmedAt,
			String ownerNote,
			boolean youthConfirmed,
			LocalDateTime youthConfirmedAt,
			String youthNote,
			boolean done,
			boolean confirmedByMe,
			@Schema(description = "올린 서류 목록")
			List<Attachment> attachments) {

		static Item from(ChecklistItem c, Role mySide, List<Attachment> attachments) {
			return new Item(c.getId(), c.getSeq(), c.getCode(), c.getTitle(), c.getDescription(),
					c.confirmedBy(Role.OWNER), c.getOwnerConfirmedAt(), c.getOwnerNote(),
					c.confirmedBy(Role.YOUTH), c.getYouthConfirmedAt(), c.getYouthNote(), c.isDone(),
					c.confirmedBy(mySide), attachments);
		}

	}

	@Schema(description = "올린 서류. 내려받기는 GET .../checklist/attachments/{id}")
	public record Attachment(Long id, String fileName, String contentType, long sizeBytes, Role uploadedBy,
			LocalDateTime uploadedAt) {
	}

}
