package com.danditon.mullyeobatgi.checklist;

import com.danditon.mullyeobatgi.user.Role;

import java.time.LocalDateTime;
import java.util.List;

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
