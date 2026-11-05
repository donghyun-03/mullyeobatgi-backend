package com.danditon.mullyeobatgi.auth;

import com.danditon.mullyeobatgi.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "auth_tokens")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AuthToken {

	@Id
	@Column(length = 64)
	private String token;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id")
	private User user;

	@Column(nullable = false)
	private LocalDateTime expiresAt;

	public AuthToken(String token, User user, LocalDateTime expiresAt) {
		this.token = token;
		this.user = user;
		this.expiresAt = expiresAt;
	}

}
