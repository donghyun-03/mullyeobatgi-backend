package com.danditon.mullyeobatgi.succession;

import com.danditon.mullyeobatgi.auth.AuthUser;
import com.danditon.mullyeobatgi.checklist.ChecklistItem;
import com.danditon.mullyeobatgi.checklist.ChecklistItemRepository;
import com.danditon.mullyeobatgi.common.ApiException;
import com.danditon.mullyeobatgi.common.Josa;
import com.danditon.mullyeobatgi.common.Money;
import com.danditon.mullyeobatgi.payment.PaymentInstallment;
import com.danditon.mullyeobatgi.payment.PaymentInstallmentRepository;
import com.danditon.mullyeobatgi.payment.PaymentStatus;
import com.danditon.mullyeobatgi.plan.Plan;
import com.danditon.mullyeobatgi.plan.PlanRepository;
import com.danditon.mullyeobatgi.roadmap.RoadmapStage;
import com.danditon.mullyeobatgi.roadmap.RoadmapStageRepository;
import com.danditon.mullyeobatgi.roadmap.RoadmapTask;
import com.danditon.mullyeobatgi.roadmap.TaskStatus;
import com.danditon.mullyeobatgi.schedule.ScheduleChangeRequest;
import com.danditon.mullyeobatgi.schedule.ScheduleChangeRequestRepository;
import com.danditon.mullyeobatgi.succession.SuccessionSummary.Action;
import com.danditon.mullyeobatgi.succession.SuccessionSummary.ActionType;
import com.danditon.mullyeobatgi.succession.SuccessionSummary.Count;
import com.danditon.mullyeobatgi.user.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * 홈 화면 요약. "확인할 일"은 두 사람 중 지금 내 차례인 것만 보여 준다.
 */
@Service
@RequiredArgsConstructor
public class SuccessionService {

	private static final int PAYMENT_DUE_SOON_DAYS = 7;

	private final SuccessionRepository successions;
	private final SuccessionAccess access;
	private final ChecklistItemRepository checklistItems;
	private final PlanRepository plans;
	private final RoadmapStageRepository roadmapStages;
	private final PaymentInstallmentRepository payments;
	private final ScheduleChangeRequestRepository changeRequests;
	private final Clock clock;

	@Transactional(readOnly = true)
	public SuccessionSummary current(AuthUser me) {
		Succession succession = successions.findLatestFor(me.id())
				.orElseThrow(() -> ApiException.notFound("아직 진행 중인 승계가 없어요"));
		return summarize(succession, succession.sideOf(me.id()));
	}

	@Transactional(readOnly = true)
	public SuccessionSummary get(Long successionId, AuthUser me) {
		Succession succession = access.get(successionId, me);
		return summarize(succession, access.side(succession, me));
	}

	private SuccessionSummary summarize(Succession s, Role side) {
		LocalDate today = LocalDate.now(clock);
		Timeline timeline = s.timeline();
		List<ChecklistItem> checklist = checklistItems.findBySuccessionIdOrderBySeq(s.getId());
		Plan plan = plans.findBySuccessionId(s.getId()).orElse(null);
		List<RoadmapStage> stages = roadmapStages.findBySuccessionIdOrderBySeq(s.getId());
		List<RoadmapTask> tasks = stages.stream().flatMap(stage -> stage.getTasks().stream()).toList();
		Integer currentStage = stages.stream().filter(stage -> !stage.isApproved()).findFirst()
				.map(RoadmapStage::getSeq).orElse(null);
		int waitingApproval = (int) tasks.stream().filter(t -> t.getStatus() == TaskStatus.REQUESTED).count();
		LocalDate planAgreedDate = plan == null || !plan.isAgreedByBoth() ? null
				: (plan.getOwnerAgreedAt().isAfter(plan.getYouthAgreedAt()) ? plan.getOwnerAgreedAt()
						: plan.getYouthAgreedAt()).toLocalDate();
		List<PaymentInstallment> pays = payments.findBySuccessionIdOrderBySeq(s.getId());

		Count checklistCount = new Count((int) checklist.stream().filter(ChecklistItem::isDone).count(), checklist.size());
		Count roadmapCount = new Count((int) tasks.stream().filter(t -> t.getStatus() == TaskStatus.APPROVED).count(),
				tasks.size());
		int paid = pays.stream().filter(p -> p.getStatus() == PaymentStatus.CONFIRMED)
				.mapToInt(PaymentInstallment::getAmount).sum();
		Count paymentCount = new Count(paid, s.getPrice());

		return new SuccessionSummary(s.getId(), s.getStatus(), s.getStatus().label(), side, s.getShop().getId(),
				s.getShop().getName(), s.getOwner().getId(), s.getOwner().getName(), s.getYouth().getId(),
				s.getYouth().getName(), s.getPrice(), s.getRetireDate(),
				Math.max(0, ChronoUnit.DAYS.between(today, s.getRetireDate())),
				progressPercent(s.getStatus(), checklistCount, plan != null, roadmapCount, paymentCount),
				checklistCount, plan != null, plan != null && plan.isAgreedByBoth(), planAgreedDate, roadmapCount,
				currentStage, waitingApproval, paymentCount,
				nextStep(s.getStatus(), timeline),
				actions(s, side, checklist, plan, tasks, pays, today));
	}

	private List<Action> actions(Succession s, Role side, List<ChecklistItem> checklist, Plan plan,
			List<RoadmapTask> tasks, List<PaymentInstallment> pays, LocalDate today) {
		List<Action> actions = new ArrayList<>();
		Role other = side == Role.OWNER ? Role.YOUTH : Role.OWNER;
		String otherName = s.nameOf(other) + (other == Role.OWNER ? " 사장님" : " 님");

		changeRequests.findBySuccessionIdOrderByCreatedAtDesc(s.getId()).stream()
				.filter(r -> r.getStatus() == ScheduleChangeRequest.Status.PENDING && r.getRequestedBy() != side)
				.forEach(r -> actions.add(new Action(ActionType.SCHEDULE_CHANGE, "일정 변경 요청",
						otherName + "이 은퇴 희망일을 바꾸자고 해요 (새 날짜 " + r.getNewRetireDate() + ")", r.getId())));

		long myUnchecked = checklist.stream().filter(c -> !c.confirmedBy(side)).count();
		if (s.getStatus() == SuccessionStatus.CHECKLIST && myUnchecked > 0) {
			actions.add(new Action(ActionType.CHECKLIST, "체크리스트 확인",
					"확인할 항목이 " + myUnchecked + "개 남았어요", null));
		}
		if (s.getStatus() == SuccessionStatus.PLANNING) {
			if (plan == null) {
				actions.add(new Action(ActionType.PLAN_CREATE, "승계 계획 만들기",
						"체크리스트를 모두 확인했어요. AI 승계 계획 초안을 만들어 보세요", null));
			} else if (!plan.agreedBy(side)) {
				actions.add(new Action(ActionType.PLAN_AGREE, "승계 계획 동의", "계획을 읽고 고치거나 동의해 주세요", plan.getId()));
			}
		}
		for (RoadmapTask t : tasks) {
			if (side == Role.OWNER && t.getStatus() == TaskStatus.REQUESTED) {
				actions.add(new Action(ActionType.ROADMAP_APPROVE, "수습 단계 승인 요청",
						otherName + "이 '" + t.getTitle() + "'를 마쳤다고 해요", t.getId()));
			}
			if (side == Role.YOUTH && t.getStatus() == TaskStatus.TODO && t.getFeedback() != null) {
				actions.add(new Action(ActionType.ROADMAP_RETRY, "다시 해 보기",
						"'" + t.getTitle() + "': " + t.getFeedback(), t.getId()));
			}
		}
		for (PaymentInstallment p : pays) {
			if (side == Role.OWNER && p.getStatus() == PaymentStatus.REPORTED) {
				actions.add(new Action(ActionType.PAYMENT_CONFIRM, "납입 확인",
						otherName + "이 " + p.getLabel() + " " + Money.format(p.getAmount()) + "을 보냈다고 기록했어요",
						p.getId()));
			}
			if (side == Role.YOUTH && p.getStatus() == PaymentStatus.SCHEDULED
					&& !p.getDueDate().isAfter(today.plusDays(PAYMENT_DUE_SOON_DAYS))) {
				actions.add(new Action(ActionType.PAYMENT_DUE, "납입일이 다가와요",
						Josa.ieyo(p.getLabel() + " " + Money.format(p.getAmount()) + ", " + p.getDueDate() + "까지"),
						p.getId()));
			}
		}
		return actions;
	}

	private static SuccessionSummary.NextStep nextStep(SuccessionStatus status, Timeline t) {
		return switch (status) {
			case CHECKLIST -> new SuccessionSummary.NextStep("체크리스트 확인", t.checklistDue());
			case PLANNING -> new SuccessionSummary.NextStep("AI 승계 계획 동의 · 계약금", t.planDue());
			case APPRENTICE -> new SuccessionSummary.NextStep("수습 마무리", t.apprenticeEnd());
			case HANDOVER -> new SuccessionSummary.NextStep("잔금 납입 · 승계 완료", t.retireDate());
			case COMPLETED -> null;
		};
	}

	private static int progressPercent(SuccessionStatus status, Count checklist, boolean planCreated, Count roadmap,
			Count payments) {
		return switch (status) {
			case CHECKLIST -> 10 + ratio(checklist, 10);
			case PLANNING -> planCreated ? 30 : 25;
			case APPRENTICE -> 35 + ratio(roadmap, 45);
			case HANDOVER -> 85 + ratio(payments, 10);
			case COMPLETED -> 100;
		};
	}

	private static int ratio(Count count, int weight) {
		return count.total() == 0 ? 0 : Math.round((float) count.done() / count.total() * weight);
	}

}
