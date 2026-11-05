package com.danditon.mullyeobatgi.youth;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface YouthProfileRepository extends JpaRepository<YouthProfile, Long> {

	Optional<YouthProfile> findByUserId(Long userId);

	boolean existsByUserId(Long userId);

	@Query("select p from YouthProfile p join fetch p.user")
	List<YouthProfile> findAllWithUser();

}
