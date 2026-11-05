package com.danditon.mullyeobatgi.succession;

import com.danditon.mullyeobatgi.offer.Offer;
import com.danditon.mullyeobatgi.shop.Shop;
import com.danditon.mullyeobatgi.user.Role;
import com.danditon.mullyeobatgi.user.User;
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
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 제안이 수락되면 시작되는 한 건의 승계. 가격과 일정은 수락 시점의 값으로 고정한다.
 */
@Entity
@Table(name = "successions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Succession {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@OneToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "offer_id")
	private Offer offer;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "shop_id")
	private Shop shop;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "owner_id")
	private User owner;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "youth_id")
	private User youth;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private SuccessionStatus status;

	@Column(nullable = false)
	private int price;

	@Column(nullable = false)
	private LocalDate acceptedDate;

	@Column(nullable = false)
	private LocalDate retireDate;

	@Column(nullable = false)
	private int apprenticeMonths;

	@Column(nullable = false)
	private LocalDateTime startedAt;

	private LocalDateTime completedAt;

	public Succession(Offer offer, LocalDate acceptedDate, LocalDateTime now) {
		this.offer = offer;
		this.shop = offer.getShop();
		this.owner = offer.getShop().getOwner();
		this.youth = offer.getYouth();
		this.price = offer.getPrice();
		this.retireDate = offer.getShop().getRetireDate();
		this.apprenticeMonths = offer.getShop().getApprenticeMonths();
		this.acceptedDate = acceptedDate;
		this.status = SuccessionStatus.CHECKLIST;
		this.startedAt = now;
	}

	public Role sideOf(Long userId) {
		if (owner.getId().equals(userId)) {
			return Role.OWNER;
		}
		if (youth.getId().equals(userId)) {
			return Role.YOUTH;
		}
		return null;
	}

	public String nameOf(Role side) {
		return side == Role.OWNER ? owner.getName() : youth.getName();
	}

	public Timeline timeline() {
		return TimelinePlanner.plan(acceptedDate, retireDate, apprenticeMonths);
	}

	public void moveTo(SuccessionStatus next) {
		this.status = next;
	}

	public void changeRetireDate(LocalDate retireDate) {
		this.retireDate = retireDate;
	}

	public void complete(LocalDateTime now) {
		this.status = SuccessionStatus.COMPLETED;
		this.completedAt = now;
	}

}
