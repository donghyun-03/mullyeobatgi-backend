package com.danditon.mullyeobatgi.checklist;

import com.danditon.mullyeobatgi.auth.AuthUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;

/**
 * ⑤ 체크리스트
 */
@Tag(name = "⑤ 체크리스트")
@RestController
@RequestMapping("/api/successions/{successionId}/checklist")
@RequiredArgsConstructor
public class ChecklistController {

	private final ChecklistService checklistService;

	@Schema(description = "체크리스트 확인 요청 (본문 생략 가능)")
	public record ConfirmRequest(
			@Schema(description = "메모. AI 승계 계획을 만들 때 참고해요", example = "건물주와 통화했어요. 계약 승계 괜찮대요")
			@Size(max = 300) String note) {
	}

	@Operation(summary = "체크리스트 보기",
			description = """
					승계 전에 **사장님과 청년이 각자 확인**할 6개 항목이에요. 제안을 수락하면 자동으로 만들어져요.

					| code | 항목 |
					|---|---|
					| `LANDLORD` | 건물주 동의 (임대차 계약 넘기기) |
					| `DEBT` | 채무와 미납금 |
					| `FUNDS` | 인수 자금 마련 |
					| `LABOR` | 수습 근로계약 |
					| `STAFF` | 직원 고용 승계 |
					| `LICENSE` | 영업 신고 명의 변경 |

					- `ownerConfirmed` / `youthConfirmed`: 양쪽 확인 상태, `confirmedByMe`: 내가 확인했는지
					- `done`: 양쪽 모두 확인함, `completed`: 6개 모두 양쪽 확인함
					- `mySide`: 이 화면을 보는 내가 OWNER 인지 YOUTH 인지
					- `dueDate`: 체크리스트 마감일 (가일정 기준). 화면의 "12월 15일까지" 표시
					- `items[].attachments`: 항목에 올린 서류 목록 (파일 이름, 크기, 올린 사람). 파일 내용은 내려받기 API로 받아요""")
	@GetMapping
	public ChecklistResponse view(@Parameter(hidden = true) @AuthenticationPrincipal AuthUser me,
			@Parameter(description = "승계 id") @PathVariable Long successionId) {
		return checklistService.view(successionId, me);
	}

	@Operation(summary = "내 쪽 확인",
			description = """
					항목 하나를 **내 쪽에서** 확인했다고 표시해요. 메모는 선택이에요 (본문을 비워도 돼요).
					6개 항목을 두 사람이 모두 확인하면 승계가 `PLANNING` 으로 넘어가고, AI 승계 계획을 만들 수 있어요.

					오류: 409 `CHECKLIST_LOCKED` 승계 계획에 동의한 뒤에는 바꿀 수 없음""")
	@PostMapping("/{itemId}/confirm")
	public ChecklistResponse confirm(@Parameter(hidden = true) @AuthenticationPrincipal AuthUser me,
			@Parameter(description = "승계 id") @PathVariable Long successionId,
			@Parameter(description = "체크리스트 항목 id") @PathVariable Long itemId,
			@Valid @RequestBody(required = false) ConfirmRequest request) {
		return checklistService.confirm(successionId, itemId, me, request == null ? null : request.note());
	}

	@Operation(summary = "서류 올리기",
			description = """
					항목에 근거 서류를 올려요. 예: `FUNDS` 인수 자금 항목에 대출 승인서, `LANDLORD` 항목에 임대차 계약서.
					`multipart/form-data` 로 `file` 필드에 담아 보내요. PDF, JPG, PNG, WEBP, HEIC 만, 5MB까지 돼요.
					두 사람 모두 올릴 수 있고, 응답의 `items[].attachments` 에 목록이 나와요.

					오류: 400 `FILE_EMPTY`, `FILE_TOO_LARGE`, `FILE_TYPE`, 409 `CHECKLIST_LOCKED` 계획 동의 후""")
	@PostMapping(path = "/{itemId}/attachments", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ChecklistResponse upload(@Parameter(hidden = true) @AuthenticationPrincipal AuthUser me,
			@Parameter(description = "승계 id") @PathVariable Long successionId,
			@Parameter(description = "체크리스트 항목 id") @PathVariable Long itemId,
			@Parameter(description = "올릴 파일") @RequestPart("file") MultipartFile file) {
		return checklistService.upload(successionId, itemId, me, file);
	}

	@Operation(summary = "서류 내려받기",
			description = "올린 서류를 원래 파일 이름 그대로 내려받아요. 승계에 참여한 두 사람 모두 볼 수 있어요.")
	@GetMapping("/attachments/{attachmentId}")
	public ResponseEntity<byte[]> download(@Parameter(hidden = true) @AuthenticationPrincipal AuthUser me,
			@Parameter(description = "승계 id") @PathVariable Long successionId,
			@Parameter(description = "서류 id (items[].attachments[].id)") @PathVariable Long attachmentId) {
		ChecklistAttachment file = checklistService.download(successionId, attachmentId, me);
		ContentDisposition disposition = ContentDisposition.attachment()
				.filename(file.getFileName(), StandardCharsets.UTF_8).build();
		return ResponseEntity.ok()
				.contentType(MediaType.parseMediaType(file.getContentType()))
				.header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
				.body(file.getData());
	}

	@Operation(summary = "서류 지우기",
			description = "내가 올린 서류만 지울 수 있어요 (아니면 403). 계획 동의 후에는 409 `CHECKLIST_LOCKED`.")
	@DeleteMapping("/attachments/{attachmentId}")
	public ChecklistResponse deleteAttachment(@Parameter(hidden = true) @AuthenticationPrincipal AuthUser me,
			@Parameter(description = "승계 id") @PathVariable Long successionId,
			@Parameter(description = "서류 id") @PathVariable Long attachmentId) {
		return checklistService.deleteAttachment(successionId, attachmentId, me);
	}

	@Operation(summary = "내 쪽 확인 취소",
			description = "내 확인을 취소해요. `PLANNING` 단계에서 취소하면 다시 `CHECKLIST` 로 돌아가요. "
					+ "계획에 동의한 뒤에는 바꿀 수 없어요 (409 `CHECKLIST_LOCKED`).")
	@DeleteMapping("/{itemId}/confirm")
	public ChecklistResponse unconfirm(@Parameter(hidden = true) @AuthenticationPrincipal AuthUser me,
			@Parameter(description = "승계 id") @PathVariable Long successionId,
			@Parameter(description = "체크리스트 항목 id") @PathVariable Long itemId) {
		return checklistService.unconfirm(successionId, itemId, me);
	}

}
