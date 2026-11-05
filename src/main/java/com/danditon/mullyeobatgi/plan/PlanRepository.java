package com.danditon.mullyeobatgi.plan;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PlanRepository extends JpaRepository<Plan, Long> {

	Optional<Plan> findBySuccessionId(Long successionId);

}
