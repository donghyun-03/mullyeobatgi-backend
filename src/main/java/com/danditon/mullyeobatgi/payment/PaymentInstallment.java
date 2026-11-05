package com.danditon.mullyeobatgi.payment;

import com.danditon.mullyeobatgi.common.ApiException;
import com.danditon.mullyeobatgi.succession.Succession;
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
 * ⑧ 인수 금액을 나눠 내는 한 번. 물려받기는 돈을 직접 옮기지 않고 기록과 확인만 한다.
 * 금액은 만 원 단위.
 */
@Entity
@Table(name = "payment_installments")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PaymentInstallment {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "succession_id")
	private Succession succession;

	@Column(nullable = false)
	private int seq;

	@Column(nullable = false, length = 50)
	private String label;

	@Column(nullable = false)
	private int amount;

	@Column(nullable = false)
	private LocalDate dueDate;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private PaymentStatus status;

	private LocalDate paidDate;

	private String memo;

	private LocalDateTime reportedAt;

	private LocalDateTime confirmedAt;

	public PaymentInstallment(Succession succession, int seq, String label, int amount, LocalDate dueDate) {
		this.succession = succession;
		this.seq = seq;
		this.label = label;
		this.amount = amount;
		this.dueDate = dueDate;
		this.status = PaymentStatus.SCHEDULED;
	}

	public void report(LocalDate paidDate, String memo, LocalDateTime now) {
		if (status == PaymentStatus.CONFIRMED) {
			throw ApiException.conflict("PAYMENT_CONFIRMED", "이미 사장님이 확인한 납입이에요");
		}
		this.status = PaymentStatus.REPORTED;
		this.paidDate = paidDate;
		this.memo = memo;
		this.reportedAt = now;
	}

	public void confirm(LocalDateTime now) {
		if (status != PaymentStatus.REPORTED) {
			throw ApiException.conflict("PAYMENT_NOT_REPORTED", "청년이 납입을 기록한 뒤에 확인할 수 있어요");
		}
		this.status = PaymentStatus.CONFIRMED;
		this.confirmedAt = now;
	}

	public void reschedule(LocalDate dueDate) {
		if (status != PaymentStatus.CONFIRMED) {
			this.dueDate = dueDate;
		}
	}

}
