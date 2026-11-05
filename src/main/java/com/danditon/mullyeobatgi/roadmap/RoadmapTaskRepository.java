package com.danditon.mullyeobatgi.roadmap;

import org.springframework.data.jpa.repository.JpaRepository;

public interface RoadmapTaskRepository extends JpaRepository<RoadmapTask, Long> {

	long countByStageSuccessionIdAndStatus(Long successionId, TaskStatus status);

}
