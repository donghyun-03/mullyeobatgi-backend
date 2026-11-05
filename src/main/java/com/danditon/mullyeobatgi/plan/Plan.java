package com.danditon.mullyeobatgi.plan;

import com.danditon.mullyeobatgi.succession.Succession;
import com.danditon.mullyeobatgi.user.Role;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * ⑥ AI 승계 계획. 두 사람이 모두 동의해야 확정된다.
 */
@Entity
@Table(name = "plans")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Plan {

	public enum Source {
		/** Claude 가 만든 초안 */
		AI,
		/** AI 키가 없거나 AI 응답이 실패해서 규칙으로 만든 초안 */
		TEMPLATE
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@OneToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "succession_id")
	private Succession succession;

	@Column(nullable = false)
	private int version;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private Source generatedBy;

	private LocalDateTime ownerAgreedAt;

	private LocalDateTime youthAgreedAt;

	@Column(nullable = false)
	private LocalDateTime createdAt;

	@Column(nullable = false)
	private LocalDateTime updatedAt;

	@OneToMany(mappedBy = "plan", cascade = CascadeType.ALL, orphanRemoval = true)
	@OrderBy("seq")
	private List<PlanSection> sections = new ArrayList<>();

	public Plan(Succession succession, LocalDateTime now) {
		this.succession = succession;
		this.version = 0;
		this.generatedBy = Source.TEMPLATE;
		this.createdAt = now;
		this.updatedAt = now;
	}

	/** 초안을 새로 만들면 섹션과 동의를 모두 새로 시작한다 */
	public void replaceDraft(Source source, List<PlanDraftSection> drafts, LocalDateTime now) {
		sections.clear();
		int seq = 1;
		for (PlanDraftSection draft : drafts) {
			sections.add(new PlanSection(this, seq++, draft.code(), draft.title(), draft.content()));
		}
		generatedBy = source;
		version++;
		ownerAgreedAt = null;
		youthAgreedAt = null;
		updatedAt = now;
	}

	/** 한쪽이 고치면 상대방의 동의는 다시 받아야 한다. 고친 사람의 동의는 그대로 둔다. */
	public void edited(Role editor, LocalDateTime now) {
		if (editor == Role.OWNER) {
			youthAgreedAt = null;
		} else {
			ownerAgreedAt = null;
		}
		version++;
		updatedAt = now;
	}

	public void agree(Role side, LocalDateTime now) {
		if (side == Role.OWNER) {
			ownerAgreedAt = now;
		} else {
			youthAgreedAt = now;
		}
		updatedAt = now;
	}

	public boolean agreedBy(Role side) {
		return (side == Role.OWNER ? ownerAgreedAt : youthAgreedAt) != null;
	}

	public boolean isAgreedByBoth() {
		return ownerAgreedAt != null && youthAgreedAt != null;
	}

}
