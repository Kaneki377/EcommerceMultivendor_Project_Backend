package com.zosh.repository;

import com.zosh.model.AffiliateRegistration;
import com.zosh.model.AffiliateCampaign;
import com.zosh.model.Koc;
import com.zosh.domain.RegistrationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AffiliateRegistrationRepository extends JpaRepository<AffiliateRegistration, Long> {

    // Kiểm tra KOC đã đăng ký campaign này chưa (dùng để tránh trùng)
    boolean existsByKocAndCampaign(Koc koc, AffiliateCampaign campaign);

    // Lấy danh sách KOC đã đăng ký 1 chiến dịch
    List<AffiliateRegistration> findByCampaign_Id(Long campaignId);

    // Lấy tất cả đăng ký cho campaign của seller
    List<AffiliateRegistration> findByCampaign_Seller_Id(Long sellerId);

    // Lấy tất cả đăng ký của 1 KOC
    List<AffiliateRegistration> findByKoc_Id(Long kocId);

    // Delete by campaign ID
    @Modifying
    @Query("DELETE FROM AffiliateRegistration ar WHERE ar.campaign.id = :campaignId")
    void deleteByCampaignId(@Param("campaignId") Long campaignId);

    // Optional: lọc theo status
    List<AffiliateRegistration> findByCampaign_Seller_IdAndStatus(Long sellerId, RegistrationStatus status);

    // Lấy tất cả registrations theo status
    List<AffiliateRegistration> findByStatus(RegistrationStatus status);
}
