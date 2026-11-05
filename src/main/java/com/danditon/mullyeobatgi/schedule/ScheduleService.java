package com.danditon.mullyeobatgi.schedule;

import com.danditon.mullyeobatgi.auth.AuthUser;
import com.danditon.mullyeobatgi.common.ApiException;
import com.danditon.mullyeobatgi.payment.PaymentFactory;
import com.danditon.mullyeobatgi.payment.PaymentInstallmentRepository;
import com.danditon.mullyeobatgi.roadmap.RoadmapStage;
import com.danditon.mullyeobatgi.roadmap.RoadmapStageRepository;
import com.danditon.mullyeobatgi.succession.Succession;
import com.danditon.mullyeobatgi.succession.SuccessionAccess;
import com.danditon.mullyeobatgi.succession.SuccessionStatus;
import com.danditon.mullyeobatgi.succession.Timeline;
import com.danditon.mullyeobatgi.user.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * ④ 가일정. 은퇴 희망일에서 거꾸로 계산하고, 바꿀 때는 상대방 승인을 받는다.
 */
@Service
@RequiredArgsConstructor
public class ScheduleService {

	private final SuccessionAccess access;
	private final ScheduleItemRepository items;
	private final ScheduleChangeRequestRepository changeRequests;
	private final RoadmapStageRepository roadmapStages;
	private final PaymentInstallmentRepository payments;
	private final Clock clock;

	@Transactional(readOnly = true)
	public ScheduleResponse view(Long successionId, AuthUser me) {
		Succession succession = access.get(successionId, me);
		Role side = access.side(succession, me);
		List<ScheduleResponse.Item> list = new ArrayList<>();
		boolean nowAssigned = false;
		for (ScheduleItem item : items.findBySuccessionIdOrderBySeq(successionId)) {
			ScheduleResponse.State state;
			if (isDone(item.getCode(), succession.getStatus())) {
				state = ScheduleResponse.State.DONE;
			} else if (!nowAssigned) {
				state = ScheduleResponse.State.NOW;
				nowAssigned = true;
			} else {
				state = ScheduleResponse.State.UPCOMING;
			}
			list.add(new ScheduleResponse.Item(item.getSeq(), item.getCode(), item.getTitle(), item.getNote(),
					item.getStartDate(), item.getEndDate(), state));
		}
		List<ScheduleResponse.ChangeRequest> requests = changeRequests
				.findBySuccessionIdOrderByCreatedAtDesc(successionId).stream()
				.map(r -> new ScheduleResponse.ChangeRequest(r.getId(), r.getRequestedBy(), r.getNewRetireDate(),
						r.getReason(), r.getStatus(), r.getCreatedAt(), r.getRequestedBy() == side))
				.toList();
		LocalDate today = LocalDate.now(clock);
		return new ScheduleResponse(succession.getRetireDate(),
				Math.max(0, ChronoUnit.DAYS.between(today, succession.getRetireDate())), list,
				succession.timeline().warnings(), requests);
	}

	@Transactional
	public ScheduleResponse requestChange(Long successionId, AuthUser me, LocalDate newRetireDate, String reason) {
		Succession succession = access.get(successionId, me);
		if (succession.getStatus() == SuccessionStatus.COMPLETED) {
			throw ApiException.conflict("SUCCESSION_COMPLETED", "이미 끝난 승계예요");
		}
		if (!newRetireDate.isAfter(LocalDate.now(clock))) {
			throw ApiException.badRequest("INVALID_DATE", "은퇴 희망일은 오늘 이후로 정해 주세요");
		}
		if (changeRequests.existsBySuccessionIdAndStatus(successionId, ScheduleChangeRequest.Status.PENDING)) {
			throw ApiException.conflict("REQUEST_PENDING", "답을 기다리는 일정 변경 요청이 이미 있어요");
		}
		changeRequests.save(new ScheduleChangeRequest(succession, access.side(succession, me), newRetireDate, reason,
				LocalDateTime.now(clock)));
		return view(successionId, me);
	}

	@Transactional
	public ScheduleResponse decide(Long successionId, Long requestId, AuthUser me, boolean approve) {
		Succession succession = access.get(successionId, me);
		ScheduleChangeRequest request = changeRequests.findById(requestId)
				.filter(r -> r.getSuccession().getId().equals(successionId))
				.orElseThrow(() -> ApiException.notFound("일정 변경 요청을 찾을 수 없어요"));
		request.decide(access.side(succession, me), approve, LocalDateTime.now(clock));
		if (approve) {
			succession.changeRetireDate(request.getNewRetireDate());
			rebuild(succession);
		}
		return view(successionId, me);
	}

	/** 바뀐 은퇴 희망일로 가일정, 로드맵 기간, 아직 확인 안 된 납입 날짜를 다시 맞춘다 */
	private void rebuild(Succession succession) {
		Timeline timeline = succession.timeline();
		items.deleteBySuccessionId(succession.getId());
		items.flush();
		items.saveAll(ScheduleItem.fromTimeline(succession, timeline));
		List<RoadmapStage> stages = roadmapStages.findBySuccessionIdOrderBySeq(succession.getId());
		for (int i = 0; i < stages.size(); i++) {
			Timeline.Period period = timeline.stages().get(i);
			stages.get(i).reschedule(period.start(), period.end());
		}
		PaymentFactory.reschedule(payments.findBySuccessionIdOrderBySeq(succession.getId()), timeline);
	}

	private static boolean isDone(String code, SuccessionStatus status) {
		return switch (code) {
			case ScheduleItem.ACCEPT -> true;
			case ScheduleItem.CHECKLIST -> status.isAfter(SuccessionStatus.CHECKLIST);
			case ScheduleItem.PLAN -> status.isAfter(SuccessionStatus.PLANNING);
			case ScheduleItem.APPRENTICE -> status.isAfter(SuccessionStatus.APPRENTICE);
			default -> status == SuccessionStatus.COMPLETED;
		};
	}

}
