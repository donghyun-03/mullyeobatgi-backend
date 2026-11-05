package com.danditon.mullyeobatgi.succession;

import com.danditon.mullyeobatgi.checklist.ChecklistItem;
import com.danditon.mullyeobatgi.checklist.ChecklistItemRepository;
import com.danditon.mullyeobatgi.offer.Offer;
import com.danditon.mullyeobatgi.schedule.ScheduleItem;
import com.danditon.mullyeobatgi.schedule.ScheduleItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * ③ 제안을 수락하면 승계를 열고 ④ 가일정과 ⑤ 체크리스트를 만든다.
 */
@Component
@RequiredArgsConstructor
public class SuccessionStarter {

	private final SuccessionRepository successions;
	private final ScheduleItemRepository scheduleItems;
	private final ChecklistItemRepository checklistItems;
	private final Clock clock;

	@Transactional
	public Succession start(Offer offer) {
		Succession succession = successions.save(
				new Succession(offer, LocalDate.now(clock), LocalDateTime.now(clock)));
		scheduleItems.saveAll(ScheduleItem.fromTimeline(succession, succession.timeline()));
		checklistItems.saveAll(ChecklistItem.defaults(succession));
		return succession;
	}

}
