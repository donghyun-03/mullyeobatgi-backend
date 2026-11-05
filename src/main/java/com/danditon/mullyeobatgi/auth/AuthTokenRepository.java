package com.danditon.mullyeobatgi.auth;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface AuthTokenRepository extends JpaRepository<AuthToken, String> {

	@Query("select t from AuthToken t join fetch t.user where t.token = :token")
	Optional<AuthToken> findWithUser(String token);

}
