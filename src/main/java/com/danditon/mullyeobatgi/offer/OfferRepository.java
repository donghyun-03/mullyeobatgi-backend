package com.danditon.mullyeobatgi.offer;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface OfferRepository extends JpaRepository<Offer, Long> {

	@Query("select o from Offer o join fetch o.youth where o.shop.id = :shopId order by o.createdAt desc")
	List<Offer> findByShopIdOrderByCreatedAtDesc(Long shopId);

	@Query("select o from Offer o join fetch o.shop s join fetch s.owner where o.youth.id = :youthId order by o.createdAt desc")
	List<Offer> findByYouthIdOrderByCreatedAtDesc(Long youthId);

	boolean existsByShopIdAndYouthIdAndStatus(Long shopId, Long youthId, OfferStatus status);

	List<Offer> findByShopIdAndStatus(Long shopId, OfferStatus status);

	List<Offer> findByYouthIdAndStatus(Long youthId, OfferStatus status);

}
