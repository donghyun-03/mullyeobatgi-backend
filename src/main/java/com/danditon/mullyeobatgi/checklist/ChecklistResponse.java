package com.danditon.mullyeobatgi.checklist;

import com.danditon.mullyeobatgi.user.Role;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "체크리스트. doneCount 는 양쪽 모두 확인한 항목 수, completed 는 모든 항목을 양쪽이 확인했는지")
public record ChecklistResponse(Role mySide, int doneCount, int total, boolean completed, List<Item> items) {

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
			boolean confirmedByMe) {

		static Item from(ChecklistItem c, Role mySide) {
			return new Item(c.getId(), c.getSeq(), c.getCode(), c.getTitle(), c.getDescription(),
					c.confirmedBy(Role.OWNER), c.getOwnerConfirmedAt(), c.getOwnerNote(),
					c.confirmedBy(Role.YOUTH), c.getYouthConfirmedAt(), c.getYouthNote(), c.isDone(),
					c.confirmedBy(mySide));
		}

	}

}
