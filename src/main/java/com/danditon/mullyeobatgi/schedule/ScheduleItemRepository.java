package com.danditon.mullyeobatgi.schedule;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ScheduleItemRepository extends JpaRepository<ScheduleItem, Long> {

	List<ScheduleItem> findBySuccessionIdOrderBySeq(Long successionId);

	void deleteBySuccessionId(Long successionId);

}
