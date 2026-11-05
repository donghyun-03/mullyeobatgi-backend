package com.danditon.mullyeobatgi.payment;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaymentInstallmentRepository extends JpaRepository<PaymentInstallment, Long> {

	List<PaymentInstallment> findBySuccessionIdOrderBySeq(Long successionId);

	boolean existsBySuccessionId(Long successionId);

}
