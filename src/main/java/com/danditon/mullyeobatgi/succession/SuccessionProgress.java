package com.danditon.mullyeobatgi.succession;

import com.danditon.mullyeobatgi.checklist.ChecklistItem;
import com.danditon.mullyeobatgi.checklist.ChecklistItemRepository;
import com.danditon.mullyeobatgi.payment.PaymentFactory;
import com.danditon.mullyeobatgi.payment.PaymentInstallmentRepository;
import com.danditon.mullyeobatgi.payment.PaymentStatus;
import com.danditon.mullyeobatgi.roadmap.RoadmapFactory;
import com.danditon.mullyeobatgi.roadmap.RoadmapStageRepository;
import com.danditon.mullyeobatgi.roadmap.RoadmapTaskRepository;
import com.danditon.mullyeobatgi.roadmap.TaskStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDateTime;

/**
 * 승계 단계를 넘기는 규칙을 한곳에 모았다.
 * 체크리스트 양쪽 확인 → 계획 동의 → 수습 단계 모두 승인 → 납입 모두 확인 → 승계 완료.
 */
@Component
@RequiredArgsConstructor
public class SuccessionProgress {

	private final ChecklistItemRepository checklistItems;
	private final RoadmapStageRepository roadmapStages;
	private final RoadmapTaskRepository roadmapTasks;
	private final PaymentInstallmentRepository payments;
	private final Clock clock;

	public boolean isChecklistDone(Succession succession) {
		return checklistItems.findBySuccessionIdOrderBySeq(succession.getId()).stream().allMatch(ChecklistItem::isDone);
	}

	/** ⑤ 체크리스트가 바뀔 때마다 계획 단계로 넘길지(또는 되돌릴지) 정한다 */
	public void afterChecklistChanged(Succession succession) {
		boolean done = isChecklistDone(succession);
		if (done && succession.getStatus() == SuccessionStatus.CHECKLIST) {
			succession.moveTo(SuccessionStatus.PLANNING);
		} else if (!done && succession.getStatus() == SuccessionStatus.PLANNING) {
			succession.moveTo(SuccessionStatus.CHECKLIST);
		}
	}

	/** ⑥ 두 사람이 계획에 동의하면 ⑦ 수습 로드맵과 ⑧ 납입 일정을 만든다 */
	public void afterPlanAgreed(Succession succession) {
		Timeline timeline = succession.timeline();
		if (!roadmapStages.existsBySuccessionId(succession.getId())) {
			roadmapStages.saveAll(RoadmapFactory.create(succession, timeline));
		}
		if (!payments.existsBySuccessionId(succession.getId())) {
			payments.saveAll(PaymentFactory.create(succession, timeline));
		}
		succession.moveTo(SuccessionStatus.APPRENTICE);
	}

	/** ⑦⑧ 수습이 모두 승인되면 운영권 넘기기로, 납입까지 모두 확인되면 승계 완료로 */
	public void refresh(Succession succession) {
		Long id = succession.getId();
		if (succession.getStatus() == SuccessionStatus.APPRENTICE
				&& roadmapTasks.countByStageSuccessionIdAndStatus(id, TaskStatus.TODO) == 0
				&& roadmapTasks.countByStageSuccessionIdAndStatus(id, TaskStatus.REQUESTED) == 0) {
			succession.moveTo(SuccessionStatus.HANDOVER);
		}
		if (succession.getStatus() == SuccessionStatus.HANDOVER
				&& payments.findBySuccessionIdOrderBySeq(id).stream()
						.allMatch(p -> p.getStatus() == PaymentStatus.CONFIRMED)) {
			succession.complete(LocalDateTime.now(clock));
		}
	}

}
