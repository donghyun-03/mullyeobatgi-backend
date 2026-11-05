package com.danditon.mullyeobatgi.roadmap;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RoadmapStageRepository extends JpaRepository<RoadmapStage, Long> {

	List<RoadmapStage> findBySuccessionIdOrderBySeq(Long successionId);

	boolean existsBySuccessionId(Long successionId);

}
