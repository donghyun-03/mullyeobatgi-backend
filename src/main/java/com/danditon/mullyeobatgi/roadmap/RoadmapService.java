package com.danditon.mullyeobatgi.roadmap;

import com.danditon.mullyeobatgi.auth.AuthUser;
import com.danditon.mullyeobatgi.common.ApiException;
import com.danditon.mullyeobatgi.succession.Succession;
import com.danditon.mullyeobatgi.succession.SuccessionAccess;
import com.danditon.mullyeobatgi.succession.SuccessionProgress;
import com.danditon.mullyeobatgi.succession.SuccessionStatus;
import com.danditon.mullyeobatgi.user.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * ⑦ 수습 로드맵. 청년이 완료를 요청하고 사장님이 단계별로 승인한다.
 */
@Service
@RequiredArgsConstructor
public class RoadmapService {

	private final SuccessionAccess access;
	private final RoadmapStageRepository stages;
	private final RoadmapTaskRepository tasks;
	private final SuccessionProgress progress;
	private final Clock clock;

	@Transactional(readOnly = true)
	public RoadmapResponse view(Long successionId, AuthUser me) {
		access.get(successionId, me);
		return response(successionId);
	}

	@Transactional
	public RoadmapResponse requestComplete(Long successionId, Long taskId, AuthUser me, String memo) {
		Succession succession = access.get(successionId, me);
		access.requireSide(succession, me, Role.YOUTH);
		requireApprenticing(succession);
		RoadmapTask task = find(successionId, taskId);
		RoadmapStage current = currentStage(stages.findBySuccessionIdOrderBySeq(successionId));
		if (current == null || !current.getId().equals(task.getStage().getId())) {
			throw ApiException.conflict("STAGE_LOCKED", "앞 단계를 먼저 마쳐 주세요");
		}
		task.requestComplete(memo, LocalDateTime.now(clock));
		return response(successionId);
	}

	@Transactional
	public RoadmapResponse approve(Long successionId, Long taskId, AuthUser me) {
		Succession succession = access.get(successionId, me);
		access.requireSide(succession, me, Role.OWNER);
		requireApprenticing(succession);
		find(successionId, taskId).approve(LocalDateTime.now(clock));
		tasks.flush();
		progress.refresh(succession);
		return response(successionId);
	}

	@Transactional
	public RoadmapResponse sendBack(Long successionId, Long taskId, AuthUser me, String feedback) {
		Succession succession = access.get(successionId, me);
		access.requireSide(succession, me, Role.OWNER);
		requireApprenticing(succession);
		find(successionId, taskId).sendBack(feedback, LocalDateTime.now(clock));
		return response(successionId);
	}

	private RoadmapTask find(Long successionId, Long taskId) {
		return tasks.findById(taskId)
				.filter(t -> t.getStage().getSuccession().getId().equals(successionId))
				.orElseThrow(() -> ApiException.notFound("로드맵 항목을 찾을 수 없어요"));
	}

	private void requireApprenticing(Succession succession) {
		if (succession.getStatus() != SuccessionStatus.APPRENTICE) {
			throw ApiException.conflict("NOT_APPRENTICING", "수습 중일 때만 할 수 있어요");
		}
	}

	private static RoadmapStage currentStage(List<RoadmapStage> list) {
		return list.stream().filter(s -> !s.isApproved()).findFirst().orElse(null);
	}

	private RoadmapResponse response(Long successionId) {
		List<RoadmapStage> list = stages.findBySuccessionIdOrderBySeq(successionId);
		if (list.isEmpty()) {
			throw ApiException.notFound("두 분이 승계 계획에 동의하면 수습 로드맵이 만들어져요");
		}
		RoadmapStage current = currentStage(list);
		List<RoadmapResponse.Stage> stageViews = new ArrayList<>();
		int approved = 0;
		int total = 0;
		int waiting = 0;
		for (RoadmapStage stage : list) {
			RoadmapResponse.StageState state = stage.isApproved() ? RoadmapResponse.StageState.DONE
					: stage == current ? RoadmapResponse.StageState.NOW : RoadmapResponse.StageState.LOCKED;
			int stageApproved = 0;
			List<RoadmapResponse.Task> taskViews = new ArrayList<>();
			for (RoadmapTask t : stage.getTasks()) {
				if (t.getStatus() == TaskStatus.APPROVED) {
					stageApproved++;
				}
				if (t.getStatus() == TaskStatus.REQUESTED) {
					waiting++;
				}
				taskViews.add(new RoadmapResponse.Task(t.getId(), t.getSeq(), t.getTitle(), t.getStatus(), t.getMemo(),
						t.getFeedback(), t.getRequestedAt(), t.getDecidedAt()));
			}
			approved += stageApproved;
			total += taskViews.size();
			stageViews.add(new RoadmapResponse.Stage(stage.getId(), stage.getSeq(), stage.getTitle(),
					stage.getDescription(), stage.getStartDate(), stage.getEndDate(), state, stageApproved, taskViews));
		}
		return new RoadmapResponse(current == null ? null : current.getSeq(), approved, total, waiting, stageViews);
	}

}
