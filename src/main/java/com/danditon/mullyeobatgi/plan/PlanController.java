package com.danditon.mullyeobatgi.plan;

import com.danditon.mullyeobatgi.auth.AuthUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * ⑥ AI 승계 계획
 */
@Tag(name = "⑥ AI 승계 계획")
@RestController
@RequestMapping("/api/successions/{successionId}/plan")
@RequiredArgsConstructor
public class PlanController {

	private final PlanService planService;

	@Schema(description = "계획 항목 고치기 요청")
	public record EditRequest(
			@Schema(description = "고친 내용", example = "2027년 1월부터 6개월 동안 주 5일, 3단계로 배워요.")
			@NotBlank @Size(max = 2000) String content) {
	}

	@Operation(summary = "승계 계획 보기",
			description = """
					지금 승계 계획과 두 사람의 동의 상태를 돌려줘요.

					- `generatedBy`: `AI` Claude 가 만든 초안, `TEMPLATE` 규칙으로 만든 초안 (AI 키가 없거나 AI 응답이 실패했을 때)
					- `version`: 초안을 다시 만들거나 고칠 때마다 올라가요
					- `sections[].editedBy`: 마지막으로 고친 사람 (OWNER / YOUTH, 안 고쳤으면 null)
					- `disclaimer`: 화면에 함께 보여 줄 안내 문장 (법적 효력 없음)

					아직 초안이 없으면 404 예요.""")
	@GetMapping
	public PlanResponse view(@Parameter(hidden = true) @AuthenticationPrincipal AuthUser me,
			@Parameter(description = "승계 id") @PathVariable Long successionId) {
		return planService.view(successionId, me);
	}

	@Operation(summary = "계획 초안 만들기",
			description = """
					체크리스트를 두 사람이 모두 확인한 뒤(`PLANNING` 단계) 둘 중 누구나 부를 수 있어요.
					이미 초안이 있으면 **새로 만들고**, 두 사람의 동의와 고친 내용은 모두 지워져요.

					초안은 5개 항목이에요: `ACQUISITION` 인수 조건, `APPRENTICE` 수습 계획, `HANDOVER` 운영권 넘기기,
					`RECIPE` 레시피와 단골, `EXIT` 그만두게 되면.
					날짜와 금액은 가일정과 인수 금액으로 계산해요. AI 키가 있으면 Claude 가 체크리스트 메모를 반영해 문장을 다듬어요.

					오류 (409): `CHECKLIST_NOT_DONE` 체크리스트가 아직 남음, `PLAN_AGREED` 이미 두 사람이 동의함""")
	@PostMapping
	public PlanResponse generate(@Parameter(hidden = true) @AuthenticationPrincipal AuthUser me,
			@Parameter(description = "승계 id") @PathVariable Long successionId) {
		return planService.generate(successionId, me);
	}

	@Operation(summary = "계획 항목 고치기",
			description = """
					항목 하나의 내용을 고쳐요. **고치면 상대방의 동의가 풀려서** 상대방이 다시 확인하고 동의해야 해요.
					고친 사람의 동의는 그대로 남아요.

					오류: 404 항목 없음, 409 `PLAN_AGREED` 이미 두 사람이 동의함""")
	@PatchMapping("/sections/{sectionId}")
	public PlanResponse edit(@Parameter(hidden = true) @AuthenticationPrincipal AuthUser me,
			@Parameter(description = "승계 id") @PathVariable Long successionId,
			@Parameter(description = "계획 항목 id (sections[].id)") @PathVariable Long sectionId,
			@Valid @RequestBody EditRequest request) {
		return planService.editSection(successionId, sectionId, me, request.content());
	}

	@Operation(summary = "계획에 동의",
			description = """
					지금 계획에 내 쪽에서 동의해요. **두 사람 모두 동의하면**
					- 승계가 `APPRENTICE`(수습) 단계로 넘어가고
					- ⑦ 수습 로드맵(3단계, 9개 일)과 ⑧ 납입 일정(계약금·중도금 2회·잔금)이 만들어져요.

					오류: 404 초안 없음, 409 `PLAN_AGREED` 이미 두 사람이 동의함""")
	@PostMapping("/agree")
	public PlanResponse agree(@Parameter(hidden = true) @AuthenticationPrincipal AuthUser me,
			@Parameter(description = "승계 id") @PathVariable Long successionId) {
		return planService.agree(successionId, me);
	}

}
