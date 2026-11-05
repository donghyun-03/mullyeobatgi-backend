package com.danditon.mullyeobatgi.offer;

import com.danditon.mullyeobatgi.auth.AuthUser;
import com.danditon.mullyeobatgi.common.ApiException;
import com.danditon.mullyeobatgi.matching.MatchScorer;
import com.danditon.mullyeobatgi.shop.Shop;
import com.danditon.mullyeobatgi.shop.ShopService;
import com.danditon.mullyeobatgi.succession.Succession;
import com.danditon.mullyeobatgi.succession.SuccessionRepository;
import com.danditon.mullyeobatgi.succession.SuccessionStarter;
import com.danditon.mullyeobatgi.user.Role;
import com.danditon.mullyeobatgi.youth.YouthProfile;
import com.danditon.mullyeobatgi.youth.YouthProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

/**
 * ③ 제안·수락. 사장님이 제안하고 청년이 수락하거나 거절한다.
 */
@Service
@RequiredArgsConstructor
public class OfferService {

	private static final int ANSWER_DAYS = 14;

	private final OfferRepository offers;
	private final ShopService shopService;
	private final YouthProfileService youthProfileService;
	private final SuccessionRepository successions;
	private final SuccessionStarter successionStarter;
	private final MatchScorer scorer;
	private final Clock clock;

	@Transactional
	public OfferResponse propose(Long ownerId, Long youthId, String message) {
		Shop shop = shopService.getByOwner(ownerId);
		YouthProfile youth = youthProfileService.getByUser(youthId);
		if (successions.existsActiveForShop(shop.getId())) {
			throw ApiException.conflict("SHOP_IN_SUCCESSION", "이미 승계를 진행 중인 가게예요");
		}
		if (successions.existsActiveForYouth(youthId)) {
			throw ApiException.conflict("YOUTH_IN_SUCCESSION", "이 청년은 이미 다른 가게와 승계를 진행 중이에요");
		}
		if (offers.existsByShopIdAndYouthIdAndStatus(shop.getId(), youthId, OfferStatus.PROPOSED)) {
			throw ApiException.conflict("OFFER_EXISTS", "이미 이 청년에게 보낸 제안이 답을 기다리고 있어요");
		}
		LocalDateTime now = LocalDateTime.now(clock);
		int score = scorer.score(shop, youth).total();
		Offer offer = offers.save(new Offer(shop, youth.getUser(), shop.getAskingPrice(), message, score, now,
				now.plusDays(ANSWER_DAYS)));
		return OfferResponse.from(offer, null);
	}

	@Transactional
	public List<OfferResponse> myOffers(AuthUser me) {
		LocalDateTime now = LocalDateTime.now(clock);
		List<Offer> list;
		if (me.role() == Role.OWNER) {
			list = offers.findByShopIdOrderByCreatedAtDesc(shopService.getByOwner(me.id()).getId());
		} else {
			me.requireRole(Role.YOUTH);
			list = offers.findByYouthIdOrderByCreatedAtDesc(me.id());
		}
		list.forEach(o -> o.expireIfDue(now));
		return list.stream().map(o -> OfferResponse.from(o, successionIdOf(o))).toList();
	}

	@Transactional
	public OfferResponse get(AuthUser me, Long offerId) {
		Offer offer = getForParticipant(me, offerId);
		offer.expireIfDue(LocalDateTime.now(clock));
		return OfferResponse.from(offer, successionIdOf(offer));
	}

	@Transactional
	public OfferResponse accept(AuthUser me, Long offerId) {
		me.requireRole(Role.YOUTH);
		Offer offer = getForParticipant(me, offerId);
		LocalDateTime now = LocalDateTime.now(clock);
		if (offer.expireIfDue(now)) {
			throw ApiException.conflict("OFFER_EXPIRED", "답할 기한이 지난 제안이에요");
		}
		if (successions.existsActiveForYouth(me.id()) || successions.existsActiveForShop(offer.getShop().getId())) {
			throw ApiException.conflict("ALREADY_IN_SUCCESSION", "이미 진행 중인 승계가 있어요");
		}
		offer.accept(now);
		// 같은 가게에 온 다른 제안과, 이 청년이 받은 다른 제안은 마감한다
		offers.findByShopIdAndStatus(offer.getShop().getId(), OfferStatus.PROPOSED).forEach(o -> o.close(now));
		offers.findByYouthIdAndStatus(me.id(), OfferStatus.PROPOSED).forEach(o -> o.close(now));
		Succession succession = successionStarter.start(offer);
		return OfferResponse.from(offer, succession.getId());
	}

	@Transactional
	public OfferResponse reject(AuthUser me, Long offerId) {
		me.requireRole(Role.YOUTH);
		Offer offer = getForParticipant(me, offerId);
		offer.reject(LocalDateTime.now(clock));
		return OfferResponse.from(offer, null);
	}

	@Transactional
	public OfferResponse cancel(AuthUser me, Long offerId) {
		me.requireRole(Role.OWNER);
		Offer offer = getForParticipant(me, offerId);
		offer.cancel(LocalDateTime.now(clock));
		return OfferResponse.from(offer, null);
	}

	private Offer getForParticipant(AuthUser me, Long offerId) {
		Offer offer = offers.findById(offerId).orElseThrow(() -> ApiException.notFound("제안을 찾을 수 없어요"));
		boolean isOwner = offer.getShop().getOwner().getId().equals(me.id());
		boolean isYouth = offer.getYouth().getId().equals(me.id());
		if (!isOwner && !isYouth) {
			throw ApiException.forbidden("내 제안이 아니에요");
		}
		return offer;
	}

	private Long successionIdOf(Offer offer) {
		if (offer.getStatus() != OfferStatus.ACCEPTED) {
			return null;
		}
		return successions.findByOfferId(offer.getId()).map(Succession::getId).orElse(null);
	}

}
