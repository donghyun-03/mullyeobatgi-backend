package com.danditon.mullyeobatgi.schedule;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ScheduleChangeRequestRepository extends JpaRepository<ScheduleChangeRequest, Long> {

	List<ScheduleChangeRequest> findBySuccessionIdOrderByCreatedAtDesc(Long successionId);

	boolean existsBySuccessionIdAndStatus(Long successionId, ScheduleChangeRequest.Status status);

}
