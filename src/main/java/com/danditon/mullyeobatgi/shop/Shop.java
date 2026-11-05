package com.danditon.mullyeobatgi.shop;

import com.danditon.mullyeobatgi.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * ① 사장님이 등록한 가게. 금액은 만 원 단위.
 */
@Entity
@Table(name = "shops")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Shop {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@OneToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "owner_id")
	private User owner;

	@Column(nullable = false, length = 100)
	private String name;

	/** 한식, 분식, 치킨처럼 청년의 희망 업종과 비교하는 값 */
	@Column(nullable = false, length = 30)
	private String category;

	private String mainMenu;

	/** 중구, 남구처럼 청년의 희망 지역과 비교하는 값 */
	@Column(nullable = false, length = 20)
	private String district;

	private String address;

	private Integer openedYear;

	private Integer monthlySales;

	@Column(nullable = false)
	private int askingPrice;

	/** 인수 금액에 포함되는 것. 예: 권리금 · 주방 설비 · 레시피 */
	private String handoverItems;

	@Column(nullable = false)
	private LocalDate retireDate;

	@Column(nullable = false)
	private int apprenticeMonths;

	private String workHours;

	/** 새벽 근무가 필요한 가게인지 */
	@Column(nullable = false)
	private boolean earlyMorning;

	private String message;

	@Column(nullable = false)
	private LocalDateTime createdAt;

	@Column(nullable = false)
	private LocalDateTime updatedAt;

	public Shop(User owner, ShopRequest request, LocalDateTime now) {
		this.owner = owner;
		this.createdAt = now;
		update(request, now);
	}

	public void update(ShopRequest request, LocalDateTime now) {
		this.name = request.name();
		this.category = request.category();
		this.mainMenu = request.mainMenu();
		this.district = request.district();
		this.address = request.address();
		this.openedYear = request.openedYear();
		this.monthlySales = request.monthlySales();
		this.askingPrice = request.askingPrice();
		this.handoverItems = request.handoverItems();
		this.retireDate = request.retireDate();
		this.apprenticeMonths = request.apprenticeMonthsOrDefault();
		this.workHours = request.workHours();
		this.earlyMorning = request.earlyMorning();
		this.message = request.message();
		this.updatedAt = now;
	}

}
