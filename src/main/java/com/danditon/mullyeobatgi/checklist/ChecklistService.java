package com.danditon.mullyeobatgi.checklist;

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
import java.util.List;

/**
 * ⑤ 체크리스트. 사장님과 청년이 각자 확인하고, 모두 양쪽 확인되면 계획 단계로 넘어간다.
 */
@Service
@RequiredArgsConstructor
public class ChecklistService {

	private final SuccessionAccess access;
	private final ChecklistItemRepository items;
	private final SuccessionProgress progress;
	private final Clock clock;

	@Transactional(readOnly = true)
	public ChecklistResponse view(Long successionId, AuthUser me) {
		Succession succession = access.get(successionId, me);
		return response(succession, access.side(succession, me));
	}

	@Transactional
	public ChecklistResponse confirm(Long successionId, Long itemId, AuthUser me, String note) {
		Succession succession = access.get(successionId, me);
		Role side = access.side(succession, me);
		requireEditable(succession);
		find(successionId, itemId).confirm(side, note, LocalDateTime.now(clock));
		progress.afterChecklistChanged(succession);
		return response(succession, side);
	}

	@Transactional
	public ChecklistResponse unconfirm(Long successionId, Long itemId, AuthUser me) {
		Succession succession = access.get(successionId, me);
		Role side = access.side(succession, me);
		requireEditable(succession);
		find(successionId, itemId).unconfirm(side);
		progress.afterChecklistChanged(succession);
		return response(succession, side);
	}

	private ChecklistItem find(Long successionId, Long itemId) {
		return items.findById(itemId)
				.filter(c -> c.getSuccession().getId().equals(successionId))
				.orElseThrow(() -> ApiException.notFound("체크리스트 항목을 찾을 수 없어요"));
	}

	private void requireEditable(Succession succession) {
		if (succession.getStatus().isAfter(SuccessionStatus.PLANNING)) {
			throw ApiException.conflict("CHECKLIST_LOCKED", "승계 계획에 동의한 뒤에는 체크리스트를 바꿀 수 없어요");
		}
	}

	private ChecklistResponse response(Succession succession, Role side) {
		List<ChecklistItem> list = items.findBySuccessionIdOrderBySeq(succession.getId());
		int done = (int) list.stream().filter(ChecklistItem::isDone).count();
		return new ChecklistResponse(side, done, list.size(), done == list.size(),
				list.stream().map(c -> ChecklistResponse.Item.from(c, side)).toList());
	}

}
