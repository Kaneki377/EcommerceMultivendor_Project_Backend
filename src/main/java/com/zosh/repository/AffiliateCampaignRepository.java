package com.zosh.repository;

import com.zosh.model.AffiliateCampaign;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AffiliateCampaignRepository extends JpaRepository<AffiliateCampaign, Long> {
    // Tìm campaign theo sellerId
    List<AffiliateCampaign> findBySellerId(Long sellerId);

    // Tìm campaign đang hoạt động
    List<AffiliateCampaign> findByActiveTrue();

    // Tìm campaign theo tên (gần đúng)
    List<AffiliateCampaign> findByNameContainingIgnoreCase(String name);
}