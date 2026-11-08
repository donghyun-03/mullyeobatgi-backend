package com.danditon.mullyeobatgi.youth;

import com.danditon.mullyeobatgi.common.StringListConverter;
import com.danditon.mullyeobatgi.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "youth_profiles")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class YouthProfile {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@OneToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id")
	private User user;

	private Integer age;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private Experience experience;

	private String experienceDetail;

	@Column(nullable = false)
	private int fundsReady;

	@Column(nullable = false)
	private boolean loanPlanned;

	@Column(nullable = false)
	private int loanAmount;

	@Convert(converter = StringListConverter.class)
	private List<String> preferredCategories;

	@Convert(converter = StringListConverter.class)
	private List<String> preferredDistricts;

	/** 새벽 근무 가능 여부. null 이면 아직 모름 */
	private Boolean earlyMorningOk;

	private String intro;

	@Column(nullable = false)
	private LocalDateTime createdAt;

	@Column(nullable = false)
	private LocalDateTime updatedAt;

	public YouthProfile(User user, YouthProfileRequest request, LocalDateTime now) {
		this.user = user;
		this.createdAt = now;
		update(request, now);
	}

	public void update(YouthProfileRequest request, LocalDateTime now) {
		this.age = request.age();
		this.experience = request.experience();
		this.experienceDetail = request.experienceDetail();
		this.fundsReady = request.fundsReady();
		this.loanPlanned = request.loanPlanned();
		this.loanAmount = request.loanPlanned() ? request.loanAmount() : 0;
		this.preferredCategories = request.preferredCategories() == null ? List.of() : request.preferredCategories();
		this.preferredDistricts = request.preferredDistricts() == null ? List.of() : request.preferredDistricts();
		this.earlyMorningOk = request.earlyMorningOk();
		this.intro = request.intro();
		this.updatedAt = now;
	}

}
