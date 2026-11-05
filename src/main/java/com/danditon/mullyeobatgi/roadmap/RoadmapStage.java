package com.danditon.mullyeobatgi.roadmap;

import com.danditon.mullyeobatgi.succession.Succession;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * ⑦ 수습 로드맵의 한 단계.
 */
@Entity
@Table(name = "roadmap_stages")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RoadmapStage {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "succession_id")
	private Succession succession;

	@Column(nullable = false)
	private int seq;

	@Column(nullable = false, length = 100)
	private String title;

	private String description;

	@Column(nullable = false)
	private LocalDate startDate;

	@Column(nullable = false)
	private LocalDate endDate;

	@OneToMany(mappedBy = "stage", cascade = CascadeType.ALL, orphanRemoval = true)
	@OrderBy("seq")
	private List<RoadmapTask> tasks = new ArrayList<>();

	public RoadmapStage(Succession succession, int seq, String title, String description, LocalDate startDate,
			LocalDate endDate, List<String> taskTitles) {
		this.succession = succession;
		this.seq = seq;
		this.title = title;
		this.description = description;
		this.startDate = startDate;
		this.endDate = endDate;
		int taskSeq = 1;
		for (String taskTitle : taskTitles) {
			tasks.add(new RoadmapTask(this, taskSeq++, taskTitle));
		}
	}

	public void reschedule(LocalDate startDate, LocalDate endDate) {
		this.startDate = startDate;
		this.endDate = endDate;
	}

	public boolean isApproved() {
		return tasks.stream().allMatch(t -> t.getStatus() == TaskStatus.APPROVED);
	}

}
