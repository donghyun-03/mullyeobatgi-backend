package com.danditon.mullyeobatgi;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * ①~⑧ 핵심 기능을 API로 처음부터 끝까지 따라가 본다.
 */
@SpringBootTest
@ActiveProfiles("test")
class SuccessionFlowTest {

	@Autowired
	private WebApplicationContext context;

	private MockMvc mvc;

	@BeforeEach
	void setUp() {
		mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
	}

	@Test
	void 로그인하지_않으면_401() throws Exception {
		mvc.perform(get("/api/me")).andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
	}

	@Test
	void Swagger_문서는_로그인_없이_열린다() throws Exception {
		mvc.perform(get("/v3/api-docs")).andExpect(status().isOk())
				.andExpect(jsonPath("$.info.title").value("물려받기 API"))
				// 로그인 사용자(AuthUser)는 요청 파라미터로 드러나지 않아야 한다
				.andExpect(jsonPath("$.paths['/api/me'].get.parameters").doesNotExist())
				.andExpect(jsonPath("$.paths['/api/offers/{offerId}/accept'].post.parameters.length()").value(1));
	}

	@Test
	void 승계_전체_흐름() throws Exception {
		String owner = login("흐름 사장님", "OWNER");
		String youth = login("흐름 청년", "YOUTH");
		String youth2 = login("흐름 청년2", "YOUTH");

		// ① 가게·청년 등록
		call(owner, put("/api/owner/shop"), shopJson(LocalDate.now().plusDays(300))).andExpect(status().isOk());
		call(youth, put("/api/youth/profile"), youthJson("ONE_TO_3Y", 3000, true, 1800, "[\"중구\"]", true))
				.andExpect(status().isOk());
		// 와이어프레임 C1 처럼 대출 금액과 새벽 근무 여부 없이도 등록된다
		call(youth2, put("/api/youth/profile"), """
				{"experience":"OVER_3Y","fundsReady":2000,"loanPlanned":true,
				 "preferredCategories":["한식"],"preferredDistricts":["수성구"]}""")
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.earlyMorningOk").doesNotExist());
		// 청년은 가게 등록을 못 한다
		call(youth, put("/api/owner/shop"), shopJson(LocalDate.now().plusDays(300))).andExpect(status().isForbidden());
		// 은퇴 희망일이 너무 가까우면 막는다
		call(owner, put("/api/owner/shop"), shopJson(LocalDate.now().plusDays(30)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("RETIRE_DATE_TOO_SOON"));

		// ② 매칭: 점수와 이유
		String matches = body(call(owner, get("/api/owner/matches"), null).andExpect(status().isOk()));
		long youthId = id(matches, "$[0].youthId");
		assertThat((String) JsonPath.read(matches, "$[0].name")).isEqualTo("흐름 청년");
		assertThat((Integer) JsonPath.read(matches, "$[0].score")).isGreaterThan(JsonPath.read(matches, "$[1].score"));
		assertThat((List<?>) JsonPath.read(matches, "$[0].reasons")).isNotEmpty();
		assertThat((Object) JsonPath.read(matches, "$[0].aiComment")).isNull();
		// 대출 금액 없이 대출 계획만 있으면 모자란 금액을 대출로 채운다고 보고, 새벽 근무는 '아직 모름'
		List<String> secondReasons = JsonPath.read(matches, "$[1].reasons[*].text");
		assertThat(secondReasons).contains("새벽 근무가 가능한지 아직 몰라요");
		// 자금 = 20 × 1.0(대출로 다 채움) + 10 × (2,000 ÷ 4,800 ÷ 0.5) ≈ 28
		assertThat((Integer) JsonPath.read(matches, "$[1].breakdown[1].points")).isEqualTo(28);

		// ③ 제안·수락
		String offer = body(call(owner, post("/api/offers"), "{\"youthId\":" + youthId + ",\"message\":\"같이 해요\"}")
				.andExpect(status().isCreated()));
		long offerId = id(offer, "$.id");
		call(owner, post("/api/offers"), "{\"youthId\":" + youthId + "}").andExpect(status().isConflict());
		call(owner, post("/api/offers/" + offerId + "/accept"), null).andExpect(status().isForbidden());
		String accepted = body(call(youth, post("/api/offers/" + offerId + "/accept"), null).andExpect(status().isOk()));
		long sid = id(accepted, "$.successionId");
		String base = "/api/successions/" + sid;

		// ④ 가일정
		call(youth, get(base + "/schedule"), null).andExpect(status().isOk())
				.andExpect(jsonPath("$.items.length()").value(6))
				.andExpect(jsonPath("$.items[0].state").value("DONE"))
				.andExpect(jsonPath("$.items[1].state").value("NOW"));
		// 다른 청년은 볼 수 없다
		call(youth2, get(base + "/schedule"), null).andExpect(status().isForbidden());

		// ⑤ 체크리스트: 양쪽이 각자 확인
		String checklist = body(call(owner, get(base + "/checklist"), null).andExpect(status().isOk()));
		List<Integer> itemIds = JsonPath.read(checklist, "$.items[*].id");
		assertThat(itemIds).hasSize(6);
		assertThat((String) JsonPath.read(checklist, "$.dueDate")).isNotBlank();
		// 서류 올리기 → 목록 → 내려받기
		MockMultipartFile pdf = new MockMultipartFile("file", "대출승인서.pdf", "application/pdf",
				"%PDF-1.4 test".getBytes());
		String uploaded = body(call(youth, multipart(base + "/checklist/" + itemIds.get(2) + "/attachments").file(pdf),
				null).andExpect(status().isOk())
				.andExpect(jsonPath("$.items[2].attachments[0].fileName").value("대출승인서.pdf"))
				.andExpect(jsonPath("$.items[2].attachments[0].uploadedBy").value("YOUTH")));
		long attachmentId = id(uploaded, "$.items[2].attachments[0].id");
		byte[] downloaded = call(owner, get(base + "/checklist/attachments/" + attachmentId), null)
				.andExpect(status().isOk()).andReturn().getResponse().getContentAsByteArray();
		assertThat(downloaded).isEqualTo("%PDF-1.4 test".getBytes());
		MockMultipartFile exe = new MockMultipartFile("file", "a.exe", "application/octet-stream", new byte[] {1});
		call(youth, multipart(base + "/checklist/" + itemIds.get(2) + "/attachments").file(exe), null)
				.andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("FILE_TYPE"));
		call(owner, delete(base + "/checklist/attachments/" + attachmentId), null).andExpect(status().isForbidden());
		call(owner, post(base + "/plan"), null).andExpect(status().isConflict());
		for (Integer itemId : itemIds) {
			call(owner, post(base + "/checklist/" + itemId + "/confirm"), "{\"note\":\"확인\"}").andExpect(status().isOk());
		}
		call(youth, get("/api/successions/current"), null)
				.andExpect(jsonPath("$.status").value("CHECKLIST"))
				.andExpect(jsonPath("$.pendingActions[0].type").value("CHECKLIST"));
		for (Integer itemId : itemIds) {
			call(youth, post(base + "/checklist/" + itemId + "/confirm"), null).andExpect(status().isOk());
		}
		call(owner, get(base), null)
				.andExpect(jsonPath("$.status").value("PLANNING"))
				.andExpect(jsonPath("$.pendingActions[0].type").value("PLAN_CREATE"));

		// ⑥ AI 승계 계획: 키가 없으면 템플릿 초안. 고치면 상대방 동의를 다시 받는다
		String plan = body(call(owner, post(base + "/plan"), null).andExpect(status().isOk())
				.andExpect(jsonPath("$.generatedBy").value("TEMPLATE"))
				.andExpect(jsonPath("$.sections.length()").value(4)));
		long sectionId = id(plan, "$.sections[1].id");
		call(owner, post(base + "/plan/agree"), null).andExpect(jsonPath("$.ownerAgreed").value(true));
		call(youth, patch(base + "/plan/sections/" + sectionId), "{\"content\":\"주 5일 근무로 바꿔요\"}")
				.andExpect(jsonPath("$.ownerAgreed").value(false))
				.andExpect(jsonPath("$.sections[1].editedBy").value("YOUTH"))
				.andExpect(jsonPath("$.sections[1].previousContent").isNotEmpty())
				.andExpect(jsonPath("$.sections[1].content").value("주 5일 근무로 바꿔요"));
		call(youth, post(base + "/plan/agree"), null).andExpect(jsonPath("$.agreedByBoth").value(false));
		call(owner, post(base + "/plan/agree"), null).andExpect(jsonPath("$.agreedByBoth").value(true));
		call(owner, get(base), null).andExpect(jsonPath("$.status").value("APPRENTICE"))
				.andExpect(jsonPath("$.planAgreedDate").isNotEmpty())
				.andExpect(jsonPath("$.roadmapCurrentStage").value(1))
				.andExpect(jsonPath("$.roadmapWaitingApproval").value(0));

		// ⑦ 수습 로드맵: 청년이 요청, 사장님이 승인
		String roadmap = body(call(youth, get(base + "/roadmap"), null).andExpect(status().isOk())
				.andExpect(jsonPath("$.stages.length()").value(3)));
		List<Integer> taskIds = JsonPath.read(roadmap, "$.stages[*].tasks[*].id");
		assertThat(taskIds).hasSize(9);
		// 앞 단계를 마치기 전에는 다음 단계 요청 불가
		call(youth, post(base + "/roadmap/tasks/" + taskIds.get(3) + "/request"), null).andExpect(status().isConflict());
		// 다시 해 보기
		call(youth, post(base + "/roadmap/tasks/" + taskIds.get(0) + "/request"), "{\"memo\":\"다 했어요\"}")
				.andExpect(status().isOk());
		call(owner, get(base), null).andExpect(jsonPath("$.pendingActions[0].type").value("ROADMAP_APPROVE"))
				.andExpect(jsonPath("$.roadmapWaitingApproval").value(1));
		call(owner, post(base + "/roadmap/tasks/" + taskIds.get(0) + "/send-back"), "{\"reason\":\"한 번 더\"}")
				.andExpect(status().isOk());
		call(youth, get(base), null).andExpect(jsonPath("$.pendingActions[0].type").value("ROADMAP_RETRY"));
		for (Integer taskId : taskIds) {
			call(youth, post(base + "/roadmap/tasks/" + taskId + "/request"), null).andExpect(status().isOk());
			call(owner, post(base + "/roadmap/tasks/" + taskId + "/approve"), null).andExpect(status().isOk());
		}
		call(owner, get(base), null).andExpect(jsonPath("$.status").value("HANDOVER"));

		// ⑧ 납입 관리: 청년이 기록, 사장님이 확인
		String payments = body(call(youth, get(base + "/payments"), null).andExpect(status().isOk())
				.andExpect(jsonPath("$.total").value(4800))
				.andExpect(jsonPath("$.installments.length()").value(4)));
		List<Integer> payIds = JsonPath.read(payments, "$.installments[*].id");
		call(owner, post(base + "/payments/" + payIds.get(0) + "/confirm"), null).andExpect(status().isConflict());
		for (Integer payId : payIds) {
			call(youth, post(base + "/payments/" + payId + "/report"), "{\"memo\":\"계좌 이체\"}")
					.andExpect(status().isOk());
			call(owner, post(base + "/payments/" + payId + "/confirm"), null).andExpect(status().isOk());
		}
		call(owner, get(base + "/payments"), null).andExpect(jsonPath("$.remainingAmount").value(0));
		call(youth, get(base), null)
				.andExpect(jsonPath("$.status").value("COMPLETED"))
				.andExpect(jsonPath("$.progressPercent").value(100));
	}

	@Test
	void 일정_변경은_상대방이_승인해야_반영된다() throws Exception {
		String owner = login("일정 사장님", "OWNER");
		String youth = login("일정 청년", "YOUTH");
		call(owner, put("/api/owner/shop"), shopJson(LocalDate.now().plusDays(300))).andExpect(status().isOk());
		String profile = body(call(youth, put("/api/youth/profile"),
				youthJson("ONE_TO_3Y", 3000, true, 1800, "[\"중구\"]", true)));
		long youthId = id(profile, "$.userId");
		long offerId = id(body(call(owner, post("/api/offers"), "{\"youthId\":" + youthId + "}")), "$.id");
		long sid = id(body(call(youth, post("/api/offers/" + offerId + "/accept"), null)), "$.successionId");
		String base = "/api/successions/" + sid + "/schedule";

		LocalDate newDate = LocalDate.now().plusDays(400);
		String requested = body(call(owner, post(base + "/change-requests"),
				"{\"retireDate\":\"" + newDate + "\",\"reason\":\"조금 더 하고 싶어요\"}").andExpect(status().isOk()));
		long requestId = id(requested, "$.changeRequests[0].id");
		// 보낸 사람은 스스로 승인할 수 없다
		call(owner, post(base + "/change-requests/" + requestId + "/approve"), null).andExpect(status().isForbidden());
		call(youth, post(base + "/change-requests/" + requestId + "/approve"), null).andExpect(status().isOk())
				.andExpect(jsonPath("$.retireDate").value(newDate.toString()))
				.andExpect(jsonPath("$.items[5].endDate").value(newDate.toString()));
	}

	private String login(String name, String role) throws Exception {
		String response = mvc.perform(post("/api/auth/dev-login").contentType(MediaType.APPLICATION_JSON)
						.content("{\"name\":\"" + name + "\",\"role\":\"" + role + "\"}"))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();
		return JsonPath.read(response, "$.token");
	}

	private ResultActions call(String token,
			org.springframework.test.web.servlet.request.AbstractMockHttpServletRequestBuilder<?> request, String json)
			throws Exception {
		request.header("Authorization", "Bearer " + token);
		if (json != null) {
			request.contentType(MediaType.APPLICATION_JSON).content(json);
		}
		return mvc.perform(request);
	}

	private static String body(ResultActions result) throws Exception {
		return result.andReturn().getResponse().getContentAsString();
	}

	private static long id(String json, String path) {
		return ((Number) JsonPath.read(json, path)).longValue();
	}

	private static String shopJson(LocalDate retireDate) {
		return """
				{"name":"남문 손칼국수","category":"한식","mainMenu":"칼국수","district":"중구","askingPrice":4800,
				 "handoverItems":"권리금 · 주방 설비 · 레시피","retireDate":"%s","apprenticeMonths":6,
				 "workHours":"새벽 5시~오후 3시","earlyMorning":true}""".formatted(retireDate);
	}

	private static String youthJson(String experience, int funds, boolean loan, int loanAmount, String districts,
			boolean earlyOk) {
		return """
				{"age":29,"experience":"%s","fundsReady":%d,"loanPlanned":%b,"loanAmount":%d,
				 "preferredCategories":["한식"],"preferredDistricts":%s,"earlyMorningOk":%b}"""
				.formatted(experience, funds, loan, loanAmount, districts, earlyOk);
	}

}
