package com.danditon.mullyeobatgi.offer;

import com.danditon.mullyeobatgi.common.ApiException;
import com.danditon.mullyeobatgi.shop.Shop;
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
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * ③ 사장님이 청년에게 보내는 승계 제안. 금액은 만 원 단위.
 */
@Entity
@Table(name = "offers")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Offer {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "shop_id")
	private Shop shop;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "youth_id")
	private User youth;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private OfferStatus status;

	@Column(nullable = false)
	private int price;

	private String message;

	@Column(nullable = false)
	private int matchScore;

	@Column(nullable = false)
	private LocalDateTime expiresAt;

	private LocalDateTime respondedAt;

	@Column(nullable = false)
	private LocalDateTime createdAt;

	public Offer(Shop shop, User youth, int price, String message, int matchScore, LocalDateTime now,
			LocalDateTime expiresAt) {
		this.shop = shop;
		this.youth = youth;
		this.price = price;
		this.message = message;
		this.matchScore = matchScore;
		this.status = OfferStatus.PROPOSED;
		this.createdAt = now;
		this.expiresAt = expiresAt;
	}

	/** 기한이 지난 제안을 만료로 바꾼다. 바뀌었으면 true */
	public boolean expireIfDue(LocalDateTime now) {
		if (status == OfferStatus.PROPOSED && now.isAfter(expiresAt)) {
			status = OfferStatus.EXPIRED;
			return true;
		}
		return false;
	}

	public void accept(LocalDateTime now) {
		requirePending();
		status = OfferStatus.ACCEPTED;
		respondedAt = now;
	}

	public void reject(LocalDateTime now) {
		requirePending();
		status = OfferStatus.REJECTED;
		respondedAt = now;
	}

	public void cancel(LocalDateTime now) {
		requirePending();
		status = OfferStatus.CANCELED;
		respondedAt = now;
	}

	public void close(LocalDateTime now) {
		if (status == OfferStatus.PROPOSED) {
			status = OfferStatus.CLOSED;
			respondedAt = now;
		}
	}

	private void requirePending() {
		if (status != OfferStatus.PROPOSED) {
			throw ApiException.conflict("OFFER_NOT_PENDING", "이미 답이 끝난 제안이에요");
		}
	}

}
