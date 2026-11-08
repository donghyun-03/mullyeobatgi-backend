package com.danditon.mullyeobatgi.plan;

import com.danditon.mullyeobatgi.user.Role;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "plan_sections")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PlanSection {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "plan_id")
	private Plan plan;

	@Column(nullable = false)
	private int seq;

	@Column(nullable = false, length = 30)
	private String code;

	@Column(nullable = false, length = 100)
	private String title;

	@Column(nullable = false, columnDefinition = "TEXT")
	private String content;

	/** 마지막으로 고치기 전 내용. 화면에서 "바뀌기 전 → 바뀐 후"로 보여 준다 */
	@Column(columnDefinition = "TEXT")
	private String previousContent;

	@Enumerated(EnumType.STRING)
	private Role editedBy;

	private LocalDateTime editedAt;

	PlanSection(Plan plan, int seq, String code, String title, String content) {
		this.plan = plan;
		this.seq = seq;
		this.code = code;
		this.title = title;
		this.content = content;
	}

	public void edit(Role editor, String content, LocalDateTime now) {
		this.previousContent = this.content;
		this.content = content;
		this.editedBy = editor;
		this.editedAt = now;
	}

}
