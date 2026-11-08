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
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * ⑤ 체크리스트. 사장님과 청년이 각자 확인하고, 모두 양쪽 확인되면 계획 단계로 넘어간다.
 * 항목마다 근거 서류(대출 승인서 등)를 올릴 수 있다.
 */
@Service
@RequiredArgsConstructor
public class ChecklistService {

	static final long MAX_FILE_BYTES = 5L * 1024 * 1024;
	private static final Set<String> ALLOWED_TYPES = Set.of("application/pdf", "image/jpeg", "image/png", "image/webp",
			"image/heic");

	private final SuccessionAccess access;
	private final ChecklistItemRepository items;
	private final ChecklistAttachmentRepository attachments;
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

	@Transactional
	public ChecklistResponse upload(Long successionId, Long itemId, AuthUser me, MultipartFile file) {
		Succession succession = access.get(successionId, me);
		Role side = access.side(succession, me);
		requireEditable(succession);
		ChecklistItem item = find(successionId, itemId);
		if (file == null || file.isEmpty()) {
			throw ApiException.badRequest("FILE_EMPTY", "올릴 파일을 골라 주세요");
		}
		if (file.getSize() > MAX_FILE_BYTES) {
			throw ApiException.badRequest("FILE_TOO_LARGE", "파일은 5MB까지 올릴 수 있어요");
		}
		String type = file.getContentType() == null ? "" : file.getContentType();
		if (!ALLOWED_TYPES.contains(type)) {
			throw ApiException.badRequest("FILE_TYPE", "PDF나 사진(JPG, PNG)만 올릴 수 있어요");
		}
		String name = file.getOriginalFilename() == null || file.getOriginalFilename().isBlank()
				? "서류" : file.getOriginalFilename();
		try {
			attachments.save(new ChecklistAttachment(item, side, truncate(name, 200), type, file.getBytes(),
					LocalDateTime.now(clock)));
		} catch (IOException e) {
			throw ApiException.badRequest("FILE_READ", "파일을 읽지 못했어요. 다시 올려 주세요");
		}
		return response(succession, side);
	}

	@Transactional(readOnly = true)
	public ChecklistAttachment download(Long successionId, Long attachmentId, AuthUser me) {
		access.get(successionId, me);
		ChecklistAttachment attachment = findAttachment(successionId, attachmentId);
		attachment.getData(); // 트랜잭션 안에서 파일 내용을 읽어 둔다
		return attachment;
	}

	@Transactional
	public ChecklistResponse deleteAttachment(Long successionId, Long attachmentId, AuthUser me) {
		Succession succession = access.get(successionId, me);
		Role side = access.side(succession, me);
		requireEditable(succession);
		ChecklistAttachment attachment = findAttachment(successionId, attachmentId);
		if (attachment.getUploadedBy() != side) {
			throw ApiException.forbidden("내가 올린 서류만 지울 수 있어요");
		}
		attachments.delete(attachment);
		return response(succession, side);
	}

	private ChecklistItem find(Long successionId, Long itemId) {
		return items.findById(itemId)
				.filter(c -> c.getSuccession().getId().equals(successionId))
				.orElseThrow(() -> ApiException.notFound("체크리스트 항목을 찾을 수 없어요"));
	}

	private ChecklistAttachment findAttachment(Long successionId, Long attachmentId) {
		return attachments.findById(attachmentId)
				.filter(a -> a.getItem().getSuccession().getId().equals(successionId))
				.orElseThrow(() -> ApiException.notFound("서류를 찾을 수 없어요"));
	}

	private void requireEditable(Succession succession) {
		if (succession.getStatus().isAfter(SuccessionStatus.PLANNING)) {
			throw ApiException.conflict("CHECKLIST_LOCKED", "승계 계획에 동의한 뒤에는 체크리스트를 바꿀 수 없어요");
		}
	}

	private ChecklistResponse response(Succession succession, Role side) {
		List<ChecklistItem> list = items.findBySuccessionIdOrderBySeq(succession.getId());
		Map<Long, List<ChecklistResponse.Attachment>> files = attachments.findInfoBySuccessionId(succession.getId())
				.stream()
				.collect(Collectors.groupingBy(ChecklistAttachmentInfo::itemId, Collectors.mapping(
						a -> new ChecklistResponse.Attachment(a.id(), a.fileName(), a.contentType(), a.sizeBytes(),
								a.uploadedBy(), a.createdAt()),
						Collectors.toList())));
		int done = (int) list.stream().filter(ChecklistItem::isDone).count();
		return new ChecklistResponse(side, succession.timeline().checklistDue(), done, list.size(), done == list.size(),
				list.stream().map(c -> ChecklistResponse.Item.from(c, side, files.getOrDefault(c.getId(), List.of())))
						.toList());
	}

	private static String truncate(String value, int max) {
		return value.length() <= max ? value : value.substring(0, max);
	}

}
