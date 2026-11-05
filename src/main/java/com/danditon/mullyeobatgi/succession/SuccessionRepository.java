package com.danditon.mullyeobatgi.succession;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SuccessionRepository extends JpaRepository<Succession, Long> {

	Optional<Succession> findByOfferId(Long offerId);

	Optional<Succession> findFirstByOwnerIdOrYouthIdOrderByIdDesc(Long ownerId, Long youthId);

	boolean existsByYouthIdAndStatusNot(Long youthId, SuccessionStatus status);

	boolean existsByShopIdAndStatusNot(Long shopId, SuccessionStatus status);

	default Optional<Succession> findLatestFor(Long userId) {
		return findFirstByOwnerIdOrYouthIdOrderByIdDesc(userId, userId);
	}

	default boolean existsActiveForYouth(Long youthId) {
		return existsByYouthIdAndStatusNot(youthId, SuccessionStatus.COMPLETED);
	}

	default boolean existsActiveForShop(Long shopId) {
		return existsByShopIdAndStatusNot(shopId, SuccessionStatus.COMPLETED);
	}

}
