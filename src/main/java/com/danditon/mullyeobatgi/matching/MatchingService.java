package com.danditon.mullyeobatgi.matching;

import com.danditon.mullyeobatgi.ai.AiAssistant;
import com.danditon.mullyeobatgi.offer.Offer;
import com.danditon.mullyeobatgi.offer.OfferRepository;
import com.danditon.mullyeobatgi.shop.Shop;
import com.danditon.mullyeobatgi.shop.ShopRepository;
import com.danditon.mullyeobatgi.shop.ShopService;
import com.danditon.mullyeobatgi.succession.SuccessionRepository;
import com.danditon.mullyeobatgi.youth.YouthProfile;
import com.danditon.mullyeobatgi.youth.YouthProfileRepository;
import com.danditon.mullyeobatgi.youth.YouthProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MatchingService {

	private static final int LIMIT = 20;
	/** AI 한마디는 비용과 응답 시간 때문에 상위 몇 명에게만 붙인다 */
	private static final int AI_COMMENT_LIMIT = 3;

	private final ShopService shopService;
	private final ShopRepository shops;
	private final YouthProfileService youthProfileService;
	private final YouthProfileRepository youthProfiles;
	private final OfferRepository offers;
	private final SuccessionRepository successions;
	private final MatchScorer scorer;
	private final AiAssistant ai;

	@Transactional(readOnly = true)
	public List<YouthCandidateResponse> candidatesFor(Long ownerId) {
		Shop shop = shopService.getByOwner(ownerId);
		Map<Long, Offer> latestOffers = offers.findByShopIdOrderByCreatedAtDesc(shop.getId()).stream()
				.collect(Collectors.toMap(o -> o.getYouth().getId(), Function.identity(), (newer, older) -> newer));

		List<ScoredYouth> scored = youthProfiles.findAllWithUser().stream()
				.filter(p -> !successions.existsActiveForYouth(p.getUser().getId()))
				.map(p -> new ScoredYouth(p, scorer.score(shop, p)))
				.sorted(Comparator.comparingInt((ScoredYouth s) -> s.score().total()).reversed())
				.limit(LIMIT)
				.toList();

		List<YouthCandidateResponse> result = new ArrayList<>();
		for (int i = 0; i < scored.size(); i++) {
			ScoredYouth s = scored.get(i);
			YouthProfile p = s.profile();
			String aiComment = i < AI_COMMENT_LIMIT ? ai.matchComment(shop, p, s.score()).orElse(null) : null;
			Offer offer = latestOffers.get(p.getUser().getId());
			result.add(new YouthCandidateResponse(p.getUser().getId(), p.getUser().getName(), p.getAge(),
					p.getExperience().label(), p.getExperienceDetail(), p.getFundsReady(), s.score().total(),
					s.score().breakdown(), s.score().reasons(), aiComment,
					offer == null ? null : offer.getId(), offer == null ? null : offer.getStatus()));
		}
		return result;
	}

	@Transactional(readOnly = true)
	public List<ShopCandidateResponse> shopsFor(Long youthId) {
		YouthProfile profile = youthProfileService.getByUser(youthId);
		return shops.findAllWithOwner().stream()
				.filter(shop -> !successions.existsActiveForShop(shop.getId()))
				.map(shop -> {
					MatchScore score = scorer.score(shop, profile);
					return new ShopCandidateResponse(shop.getId(), shop.getName(), shop.getOwner().getName(),
							shop.getCategory(), shop.getMainMenu(), shop.getDistrict(), shop.getOpenedYear(),
							shop.getAskingPrice(), shop.getRetireDate(), score.total(), score.breakdown(),
							score.reasons());
				})
				.sorted(Comparator.comparingInt(ShopCandidateResponse::score).reversed())
				.limit(LIMIT)
				.toList();
	}

	private record ScoredYouth(YouthProfile profile, MatchScore score) {
	}

}
