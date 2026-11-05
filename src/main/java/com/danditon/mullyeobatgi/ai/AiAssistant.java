package com.danditon.mullyeobatgi.ai;

import com.danditon.mullyeobatgi.checklist.ChecklistItem;
import com.danditon.mullyeobatgi.common.Money;
import com.danditon.mullyeobatgi.matching.MatchScore;
import com.danditon.mullyeobatgi.plan.PlanDraftSection;
import com.danditon.mullyeobatgi.shop.Shop;
import com.danditon.mullyeobatgi.succession.Succession;
import com.danditon.mullyeobatgi.youth.YouthProfile;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Claude 연결. AI_CHAT_MODEL=anthropic 과 ANTHROPIC_API_KEY 가 있으면 ChatModel 이 생기고,
 * 없으면 모든 메서드가 빈 값을 돌려줘서 호출한 쪽이 규칙·템플릿 결과를 그대로 쓴다.
 * AI 응답이 실패해도 서비스가 멈추지 않도록 예외는 삼키고 기록만 한다.
 */
@Slf4j
@Component
public class AiAssistant {

	private static final String MATCH_SYSTEM = """
			너는 노포 승계 플랫폼 '물려받기'의 매칭 도우미야.
			은퇴를 앞둔 사장님이 청년 후보를 보고 판단할 수 있게, 주어진 점수와 이유만 근거로 한두 문장(80자 이내)으로 설명해.
			해요체로 쓰고, 주어진 정보에 없는 사실은 지어내지 마.""";

	private static final String PLAN_SYSTEM = """
			너는 노포 승계 플랫폼 '물려받기'의 승계 계획 도우미야.
			기준 초안을 사장님과 청년이 읽기 쉽게 다듬어. 규칙:
			- 섹션 수, 순서, code, title 은 기준 초안 그대로 둔다.
			- 날짜와 금액은 기준 초안의 값을 바꾸지 않는다.
			- 체크리스트에 남긴 메모가 있으면 관련 섹션에 반영한다.
			- 각 content 는 3문장 이내, 해요체, 60대 사장님도 바로 이해할 쉬운 말로 쓴다.
			- 법률 자문처럼 단정하지 않는다.""";

	private final ChatClient chatClient;

	public AiAssistant(ObjectProvider<ChatModel> chatModel) {
		ChatModel model = chatModel.getIfAvailable();
		this.chatClient = model == null ? null : ChatClient.create(model);
		log.info(model == null ? "AI 키가 없어 템플릿으로 동작합니다" : "Claude 연결됨");
	}

	public boolean enabled() {
		return chatClient != null;
	}

	public Optional<String> matchComment(Shop shop, YouthProfile youth, MatchScore score) {
		if (!enabled()) {
			return Optional.empty();
		}
		String reasons = score.reasons().stream().map(r -> "- " + r.text()).collect(Collectors.joining("\n"));
		String user = """
				가게: %s (%s, %s, 인수 금액 %s)
				청년: %s, %s, 준비된 자금 %s
				적합도: %d점
				이유:
				%s""".formatted(shop.getName(), shop.getCategory(), shop.getDistrict(),
				Money.format(shop.getAskingPrice()), youth.getUser().getName(), youth.getExperience().label(),
				Money.format(youth.getFundsReady()), score.total(), reasons);
		try {
			String content = chatClient.prompt().system(MATCH_SYSTEM).user(user).call().content();
			return Optional.ofNullable(content).map(String::strip).filter(s -> !s.isEmpty());
		} catch (RuntimeException e) {
			log.warn("AI 매칭 설명 실패, 규칙 결과만 보냅니다: {}", e.getMessage());
			return Optional.empty();
		}
	}

	public Optional<List<PlanDraftSection>> draftPlan(Succession succession, List<ChecklistItem> checklist,
			List<PlanDraftSection> baseline) {
		if (!enabled()) {
			return Optional.empty();
		}
		String notes = checklist.stream()
				.filter(c -> c.getOwnerNote() != null || c.getYouthNote() != null)
				.map(c -> "- " + c.getTitle() + ": 사장님 메모=" + nullToDash(c.getOwnerNote()) + ", 청년 메모="
						+ nullToDash(c.getYouthNote()))
				.collect(Collectors.joining("\n"));
		String draft = baseline.stream()
				.map(s -> "[" + s.code() + "] " + s.title() + "\n" + s.content())
				.collect(Collectors.joining("\n\n"));
		String user = """
				가게: %s (%s, 대표 메뉴 %s)
				사장님: %s / 청년: %s
				체크리스트 메모:
				%s

				기준 초안:
				%s""".formatted(succession.getShop().getName(), succession.getShop().getCategory(),
				nullToDash(succession.getShop().getMainMenu()), succession.getOwner().getName(),
				succession.getYouth().getName(), notes.isEmpty() ? "(없음)" : notes, draft);
		try {
			PlanDraft result = chatClient.prompt().system(PLAN_SYSTEM).user(user).call().entity(PlanDraft.class);
			if (result == null || !sameShape(baseline, result.sections())) {
				log.warn("AI 승계 계획이 기준 초안과 형식이 달라 템플릿을 씁니다");
				return Optional.empty();
			}
			return Optional.of(result.sections());
		} catch (RuntimeException e) {
			log.warn("AI 승계 계획 생성 실패, 템플릿을 씁니다: {}", e.getMessage());
			return Optional.empty();
		}
	}

	private static boolean sameShape(List<PlanDraftSection> baseline, List<PlanDraftSection> sections) {
		if (sections == null || sections.size() != baseline.size()) {
			return false;
		}
		for (int i = 0; i < baseline.size(); i++) {
			PlanDraftSection s = sections.get(i);
			if (!baseline.get(i).code().equals(s.code()) || s.content() == null || s.content().isBlank()) {
				return false;
			}
		}
		return true;
	}

	private static String nullToDash(String value) {
		return value == null || value.isBlank() ? "-" : value;
	}

	record PlanDraft(List<PlanDraftSection> sections) {
	}

}
