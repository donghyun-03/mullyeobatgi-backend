package com.danditon.mullyeobatgi.schedule;

import com.danditon.mullyeobatgi.common.ApiException;
import com.danditon.mullyeobatgi.succession.Succession;
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

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * ④ 가일정은 한쪽이 혼자 바꾸지 못한다. 변경을 요청하면 상대방이 승인해야 반영된다.
 */
@Entity
@Table(name = "schedule_change_requests")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ScheduleChangeRequest {

	public enum Status {
		PENDING, APPROVED, REJECTED
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "succession_id")
	private Succession succession;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private Role requestedBy;

	@Column(nullable = false)
	private LocalDate newRetireDate;

	private String reason;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private Status status;

	@Column(nullable = false)
	private LocalDateTime createdAt;

	private LocalDateTime decidedAt;

	public ScheduleChangeRequest(Succession succession, Role requestedBy, LocalDate newRetireDate, String reason,
			LocalDateTime now) {
		this.succession = succession;
		this.requestedBy = requestedBy;
		this.newRetireDate = newRetireDate;
		this.reason = reason;
		this.status = Status.PENDING;
		this.createdAt = now;
	}

	public void decide(Role decider, boolean approve, LocalDateTime now) {
		if (status != Status.PENDING) {
			throw ApiException.conflict("REQUEST_DECIDED", "이미 답한 요청이에요");
		}
		if (decider == requestedBy) {
			throw ApiException.forbidden("내가 보낸 요청은 상대방이 답해야 해요");
		}
		status = approve ? Status.APPROVED : Status.REJECTED;
		decidedAt = now;
	}

}
