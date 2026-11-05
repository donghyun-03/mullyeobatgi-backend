package com.danditon.mullyeobatgi.plan;

import com.danditon.mullyeobatgi.succession.Succession;
import com.danditon.mullyeobatgi.user.Role;

import java.time.LocalDateTime;
import java.util.List;

public record PlanResponse(
		Long id,
		int version,
		Plan.Source generatedBy,
		Role mySide,
		boolean ownerAgreed,
		LocalDateTime ownerAgreedAt,
		boolean youthAgreed,
		LocalDateTime youthAgreedAt,
		boolean agreedByBoth,
		List<Section> sections,
		String disclaimer) {

	public static final String DISCLAIMER = "AI가 만든 초안이라 법적 효력이 없어요. 실제 계약 전에 전문가 검토를 받으세요.";

	public record Section(Long id, int seq, String code, String title, String content, Role editedBy,
			String editedByName, LocalDateTime editedAt) {
	}

	public static PlanResponse from(Plan plan, Succession succession, Role mySide) {
		List<Section> sections = plan.getSections().stream()
				.map(s -> new Section(s.getId(), s.getSeq(), s.getCode(), s.getTitle(), s.getContent(),
						s.getEditedBy(), s.getEditedBy() == null ? null : succession.nameOf(s.getEditedBy()),
						s.getEditedAt()))
				.toList();
		return new PlanResponse(plan.getId(), plan.getVersion(), plan.getGeneratedBy(), mySide,
				plan.agreedBy(Role.OWNER), plan.getOwnerAgreedAt(), plan.agreedBy(Role.YOUTH),
				plan.getYouthAgreedAt(), plan.isAgreedByBoth(), sections, DISCLAIMER);
	}

}
