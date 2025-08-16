package com.zosh.repository;


import com.zosh.domain.PayoutStatus;
import com.zosh.model.Payout;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PayoutRepository extends JpaRepository<Payout, Integer> {

    List<Payout> findByCampaign_Seller_Id(Long sellerId);
    List<Payout> findByKoc_Id(Long kocId);
    List<Payout> findByCampaign_Seller_IdAndStatus(Long sellerId, PayoutStatus status);
}
