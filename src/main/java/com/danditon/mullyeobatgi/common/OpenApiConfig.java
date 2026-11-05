package com.danditon.mullyeobatgi.common;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Swagger 문서: http://localhost:8080/swagger-ui.html
 */
@Configuration
public class OpenApiConfig {

	static final String BEARER = "bearerAuth";

	private static final String DESCRIPTION = """
			문 닫을 대구 노포의 맛과 단골을 청년이 이어받게 하는 승계 서비스 **물려받기**의 API예요.

			### 시작하는 법
			1. **개발용 로그인** `POST /api/auth/dev-login` 을 실행해 `token` 을 받아요.
			   예시 데이터: 사장님 `김순자`(OWNER), 청년 `이도윤`·`박서연`·`최민재`(YOUTH)
			2. 오른쪽 위 **Authorize** 버튼에 토큰을 넣어요 (`Bearer` 없이 토큰만).
			3. 이후 모든 요청에 `Authorization: Bearer {token}` 이 붙어요.

			### 공통 규칙
			- 금액은 모두 **만 원 단위 정수**예요. 예: `4800` = 4,800만 원
			- 날짜는 `YYYY-MM-DD`, 시각은 `YYYY-MM-DDTHH:mm:ss` (한국 시간)
			- 오류는 `{"code": "...", "message": "..."}` 형식이고, `message` 는 화면에 그대로 보여 줄 수 있는 한국어 문장이에요.
			  - 400 `INVALID_INPUT` 입력값 오류, 401 `UNAUTHORIZED` 로그인 필요, 403 `FORBIDDEN` 권한 없음,
			    404 `NOT_FOUND` 없음, 409 상태가 맞지 않음(코드는 API마다 달라요)

			### 승계 흐름
			`CHECKLIST`(체크리스트) → `PLANNING`(계획 동의) → `APPRENTICE`(수습) → `HANDOVER`(운영권 넘기기) → `COMPLETED`(완료)

			중요한 결정은 한쪽이 혼자 끝내지 않아요. 제안과 수락, 체크리스트 양쪽 확인, 계획 동의, 단계 승인, 납입 확인처럼
			두 사람이 각자 확인해야 다음 단계로 넘어가요.""";

	@Bean
	public OpenAPI openApi() {
		return new OpenAPI()
				.info(new Info().title("물려받기 API").version("v1").description(DESCRIPTION))
				.components(new Components().addSecuritySchemes(BEARER, new SecurityScheme()
						.type(SecurityScheme.Type.HTTP).scheme("bearer")
						.description("로그인 API에서 받은 토큰")))
				.addSecurityItem(new SecurityRequirement().addList(BEARER))
				.tags(List.of(
						tag("0. 로그인·내 정보", "로그인, 로그아웃, 내 정보, 역할 선택"),
						tag("① 가게·청년 등록", "사장님은 가게 정보, 청년은 경력·자금·희망 조건을 입력해요"),
						tag("② 매칭", "가게와 청년의 적합도 점수와 그 이유"),
						tag("③ 제안·수락", "사장님이 제안하고 청년이 수락하거나 거절해요"),
						tag("홈 · 승계 요약", "진행률과 지금 내가 해야 할 일"),
						tag("④ 가일정", "은퇴 희망일에서 거꾸로 계산한 승계 일정과 변경 요청"),
						tag("⑤ 체크리스트", "건물주 동의, 채무, 자금, 근로계약 등을 양쪽이 각자 확인"),
						tag("⑥ AI 승계 계획", "AI 초안을 두 사람이 고치고 동의"),
						tag("⑦ 수습 로드맵", "청년이 완료를 요청하고 사장님이 단계별로 승인"),
						tag("⑧ 납입 관리", "인수 금액 납입 기록과 남은 금액")));
	}

	private static Tag tag(String name, String description) {
		return new Tag().name(name).description(description);
	}

}
