package com.danditon.mullyeobatgi.plan;

import com.danditon.mullyeobatgi.ai.AiAssistant;
import com.danditon.mullyeobatgi.auth.AuthUser;
import com.danditon.mullyeobatgi.checklist.ChecklistItemRepository;
import com.danditon.mullyeobatgi.common.ApiException;
import com.danditon.mullyeobatgi.succession.Succession;
import com.danditon.mullyeobatgi.succession.SuccessionAccess;
import com.danditon.mullyeobatgi.succession.SuccessionProgress;
import com.danditon.mullyeobatgi.succession.SuccessionStatus;
import com.danditon.mullyeobatgi.user.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * ⑥ AI 승계 계획. 체크리스트를 모두 확인하면 AI(없으면 템플릿)가 초안을 만들고,
 * 두 사람이 고친 뒤 모두 동의하면 수습이 시작된다.
 */
@Service
@RequiredArgsConstructor
public class PlanService {

	private final SuccessionAccess access;
	private final PlanRepository plans;
	private final ChecklistItemRepository checklistItems;
	private final SuccessionProgress progress;
	private final AiAssistant ai;
	private final Clock clock;

	@Transactional(readOnly = true)
	public PlanResponse view(Long successionId, AuthUser me) {
		Succession succession = access.get(successionId, me);
		Plan plan = plans.findBySuccessionId(successionId)
				.orElseThrow(() -> ApiException.notFound("아직 승계 계획이 없어요. 체크리스트를 마치면 만들 수 있어요"));
		return PlanResponse.from(plan, succession, access.side(succession, me));
	}

	/** 초안 만들기. 동의 전이면 다시 만들 수 있다 */
	@Transactional
	public PlanResponse generate(Long successionId, AuthUser me) {
		Succession succession = access.get(successionId, me);
		requirePlanning(succession);
		List<PlanDraftSection> baseline = PlanTemplate.build(succession, succession.timeline());
		Optional<List<PlanDraftSection>> aiDraft = ai.draftPlan(succession,
				checklistItems.findBySuccessionIdOrderBySeq(successionId), baseline);
		LocalDateTime now = LocalDateTime.now(clock);
		Plan plan = plans.findBySuccessionId(successionId).orElseGet(() -> plans.save(new Plan(succession, now)));
		plan.replaceDraft(aiDraft.isPresent() ? Plan.Source.AI : Plan.Source.TEMPLATE, aiDraft.orElse(baseline), now);
		plans.flush();
		return PlanResponse.from(plan, succession, access.side(succession, me));
	}

	@Transactional
	public PlanResponse editSection(Long successionId, Long sectionId, AuthUser me, String content) {
		Succession succession = access.get(successionId, me);
		requirePlanning(succession);
		Role side = access.side(succession, me);
		Plan plan = getPlan(successionId);
		PlanSection section = plan.getSections().stream()
				.filter(s -> s.getId().equals(sectionId))
				.findFirst()
				.orElseThrow(() -> ApiException.notFound("계획 항목을 찾을 수 없어요"));
		LocalDateTime now = LocalDateTime.now(clock);
		section.edit(side, content, now);
		plan.edited(side, now);
		return PlanResponse.from(plan, succession, side);
	}

	@Transactional
	public PlanResponse agree(Long successionId, AuthUser me) {
		Succession succession = access.get(successionId, me);
		requirePlanning(succession);
		Role side = access.side(succession, me);
		Plan plan = getPlan(successionId);
		plan.agree(side, LocalDateTime.now(clock));
		if (plan.isAgreedByBoth()) {
			progress.afterPlanAgreed(succession);
		}
		return PlanResponse.from(plan, succession, side);
	}

	private Plan getPlan(Long successionId) {
		return plans.findBySuccessionId(successionId)
				.orElseThrow(() -> ApiException.notFound("먼저 승계 계획 초안을 만들어 주세요"));
	}

	private void requirePlanning(Succession succession) {
		if (succession.getStatus() == SuccessionStatus.CHECKLIST) {
			throw ApiException.conflict("CHECKLIST_NOT_DONE", "체크리스트를 두 분이 모두 확인해야 계획을 만들 수 있어요");
		}
		if (succession.getStatus() != SuccessionStatus.PLANNING) {
			throw ApiException.conflict("PLAN_AGREED", "두 분이 이미 동의한 계획이에요");
		}
	}

}
