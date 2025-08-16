package com.zosh.repository;

import com.zosh.model.AffiliateLink;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface AffiliateLinkRepository extends JpaRepository<AffiliateLink, Long> {

    List<AffiliateLink> findByKoc_Id(Long kocId);
    List<AffiliateLink> findByCampaign_Id(Long campaignId);

    // Kiểm tra trùng (khi product != null)
    Optional<AffiliateLink> findByKoc_IdAndCampaign_IdAndProduct_Id(Long kocId, Long campaignId, Long productId);

    // Kiểm tra trùng (link toàn campaign => product IS NULL)
    Optional<AffiliateLink> findByKoc_IdAndCampaign_IdAndProduct_IsNull(Long kocId, Long campaignId);

    Optional<AffiliateLink> findByShortToken(String shortToken);

    // Tăng click đếm an toàn
    @Modifying
    @Transactional
    @Query("update AffiliateLink l set l.totalClick = l.totalClick + 1 where l.id = :id")
    void incrementClick(@Param("id") Long id);
}
