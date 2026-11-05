package com.danditon.mullyeobatgi.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 50)
	private String name;

	/** 카카오로 처음 들어오면 역할을 고르기 전까지 비어 있다 */
	@Enumerated(EnumType.STRING)
	private Role role;

	private Long kakaoId;

	@Column(nullable = false)
	private LocalDateTime createdAt;

	public User(String name, Role role, Long kakaoId, LocalDateTime now) {
		this.name = name;
		this.role = role;
		this.kakaoId = kakaoId;
		this.createdAt = now;
	}

	public void chooseRole(Role role) {
		this.role = role;
	}

}
