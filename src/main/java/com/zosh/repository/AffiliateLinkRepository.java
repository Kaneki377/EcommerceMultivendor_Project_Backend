package com.zosh.repository;

import com.zosh.model.AffiliateLink;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AffiliateLinkRepository extends JpaRepository<AffiliateLink, Long> {

    Optional<AffiliateLink> findByCode(String code);
    List<AffiliateLink> findByKoc_Id(Long kocId);
    List<AffiliateLink> findByCampaign_Id(Long campaignId);
}
