package com.danditon.mullyeobatgi.matching;

import com.danditon.mullyeobatgi.common.Josa;
import com.danditon.mullyeobatgi.common.Money;
import com.danditon.mullyeobatgi.matching.MatchScore.Part;
import com.danditon.mullyeobatgi.matching.MatchScore.Reason;
import com.danditon.mullyeobatgi.matching.MatchScore.ReasonType;
import com.danditon.mullyeobatgi.shop.Shop;
import com.danditon.mullyeobatgi.youth.Experience;
import com.danditon.mullyeobatgi.youth.YouthProfile;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 점수는 규칙으로 계산한다. 같은 조건이면 언제나 같은 점수가 나와야 시연 중에도 믿을 수 있다.
 * 경력 30점 + 자금 30점 + 조건 40점(업종 15, 지역 15, 근무 시간 10).
 */
@Component
public class MatchScorer {

	static final int EXPERIENCE_MAX = 30;
	static final int FUNDS_MAX = 30;
	static final int CONDITIONS_MAX = 40;

	public MatchScore score(Shop shop, YouthProfile youth) {
		List<Reason> reasons = new ArrayList<>();
		int experience = experience(youth, reasons);
		int conditions = conditions(shop, youth, reasons);
		int funds = funds(shop, youth, reasons);
		List<Part> breakdown = List.of(
				part("경력", experience, EXPERIENCE_MAX),
				part("자금", funds, FUNDS_MAX),
				part("조건", conditions, CONDITIONS_MAX));
		// 좋은 이유를 먼저 보여 준다
		reasons.sort((a, b) -> a.type().compareTo(b.type()));
		return new MatchScore(experience + funds + conditions, breakdown, reasons);
	}

	private int experience(YouthProfile youth, List<Reason> reasons) {
		Experience exp = youth.getExperience();
		String detail = youth.getExperienceDetail() == null ? "" : " (" + youth.getExperienceDetail() + ")";
		switch (exp) {
			case NONE -> reasons.add(new Reason(ReasonType.CAUTION, "음식점 경력이 없어요. 수습 기간에 더 많이 배워야 해요"));
			case UNDER_1Y -> reasons.add(new Reason(ReasonType.CAUTION, "음식점 경력이 1년 미만이에요" + detail));
			default -> reasons.add(new Reason(ReasonType.GOOD, "음식점 " + exp.label() + "이에요" + detail));
		}
		return exp.points();
	}

	private int conditions(Shop shop, YouthProfile youth, List<Reason> reasons) {
		int points = 0;
		if (youth.getPreferredCategories().contains(shop.getCategory())) {
			points += 15;
			reasons.add(new Reason(ReasonType.GOOD, "희망 업종(" + shop.getCategory() + ")과 맞아요"));
		} else {
			reasons.add(new Reason(ReasonType.CAUTION, "희망 업종에 " + Josa.iGa(shop.getCategory()) + " 없어요"));
		}

		if (youth.getPreferredDistricts().contains(shop.getDistrict())) {
			points += 15;
			reasons.add(new Reason(ReasonType.GOOD, shop.getDistrict() + "에서 가게를 찾고 있어요"));
		} else if (youth.getPreferredDistricts().isEmpty()) {
			points += 8;
		} else {
			reasons.add(new Reason(ReasonType.CAUTION,
					"희망 지역이 " + Josa.ieyo(String.join(", ", youth.getPreferredDistricts()))));
		}

		if (!shop.isEarlyMorning()) {
			points += 10;
		} else if (youth.getEarlyMorningOk() == null) {
			points += 5;
			reasons.add(new Reason(ReasonType.CAUTION, "새벽 근무가 가능한지 아직 몰라요"));
		} else if (youth.getEarlyMorningOk()) {
			points += 10;
			reasons.add(new Reason(ReasonType.GOOD, "새벽 근무가 가능해요"));
		} else {
			reasons.add(new Reason(ReasonType.CAUTION, "새벽 근무가 어렵다고 했어요"));
		}
		return points;
	}

	private int funds(Shop shop, YouthProfile youth, List<Reason> reasons) {
		int price = shop.getAskingPrice();
		int ready = youth.getFundsReady();
		// 대출 계획만 있고 금액을 안 적었으면 모자란 만큼 대출로 채운다고 본다
		int loan = !youth.isLoanPlanned() ? 0
				: youth.getLoanAmount() > 0 ? youth.getLoanAmount() : Math.max(0, price - ready);
		double coverage = Math.min(1.0, (double) (ready + loan) / price);
		double ownShare = Math.min(1.0, (double) ready / price / 0.5);
		int points = (int) Math.round(20 * coverage + 10 * ownShare);

		int shortfall = price - ready;
		if (shortfall <= 0) {
			reasons.add(new Reason(ReasonType.GOOD, "인수 금액 " + Money.format(price) + "을 준비된 자금으로 낼 수 있어요"));
		} else if (loan >= shortfall) {
			reasons.add(new Reason(ReasonType.CAUTION, "자금 " + Money.format(ready) + "에 "
					+ Money.format(shortfall) + "이 더 필요해요. 대출 계획이 있어요"));
		} else {
			reasons.add(new Reason(ReasonType.CAUTION, "자금 " + Money.format(ready) + "에 "
					+ Money.format(shortfall) + "이 더 필요해요"));
		}
		return points;
	}

	private Part part(String name, int points, int max) {
		double ratio = (double) points / max;
		String level = ratio >= 0.8 ? "높음" : ratio >= 0.5 ? "보통" : "낮음";
		return new Part(name, points, max, level);
	}

}
