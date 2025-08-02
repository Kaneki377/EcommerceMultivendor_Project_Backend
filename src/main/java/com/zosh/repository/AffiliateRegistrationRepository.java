package com.zosh.repository;

import com.zosh.domain.RegistrationStatus;
import com.zosh.model.AffiliateRegistration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AffiliateRegistrationRepository extends JpaRepository<AffiliateRegistration, Long> {
    // Tìm registration theo kocId
    List<AffiliateRegistration> findByKocId(Long kocId);

    // Tìm registration theo campaignId
    List<AffiliateRegistration> findByCampaignId(Long campaignId);

    // Tìm registration theo trạng thái
    List<AffiliateRegistration> findByStatus(RegistrationStatus status);
}