package com.zosh.repository;

import com.zosh.model.AffiliateLink;
import com.zosh.model.AffiliateCampaign;
import com.zosh.model.Koc;
import com.zosh.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AffiliateLinkRepository extends JpaRepository<AffiliateLink, Long> {

    // Tìm link theo shortToken (để redirect)
    Optional<AffiliateLink> findByShortToken(String shortToken);

    // Tìm tất cả link của một KOC
    List<AffiliateLink> findByKoc_Id(Long kocId);

    // Tìm link của KOC trong một campaign cụ thể
    List<AffiliateLink> findByKoc_IdAndCampaign_Id(Long kocId, Long campaignId);

    // Tìm link cho một product cụ thể của KOC
    Optional<AffiliateLink> findByKoc_IdAndProduct_Id(Long kocId, Long productId);

    // Tìm link campaign tổng (không có product cụ thể)
    Optional<AffiliateLink> findByKoc_IdAndCampaign_IdAndProductIsNull(Long kocId, Long campaignId);

    // Kiểm tra KOC đã có link cho campaign này chưa
    boolean existsByKocAndCampaign(Koc koc, AffiliateCampaign campaign);

    // Kiểm tra KOC đã có link cho product này chưa
    boolean existsByKocAndProduct(Koc koc, Product product);

    // Tìm affiliate link theo KOC code, campaign code và product ID (cho affiliate
    // tracking)
    Optional<AffiliateLink> findByKoc_KocCodeAndCampaign_CampaignCodeAndProduct_Id(
            String kocCode, String campaignCode, Long productId);

    // Lấy top link có nhiều click nhất
    @Query("SELECT al FROM AffiliateLink al ORDER BY al.totalClick DESC")
    List<AffiliateLink> findTopByOrderByTotalClickDesc();

    // Thống kê click theo campaign
    @Query("SELECT SUM(al.totalClick) FROM AffiliateLink al WHERE al.campaign.id = :campaignId")
    Long getTotalClicksByCampaign(@Param("campaignId") Long campaignId);

    // Thống kê click theo KOC
    @Query("SELECT SUM(al.totalClick) FROM AffiliateLink al WHERE al.koc.id = :kocId")
    Long getTotalClicksByKoc(@Param("kocId") Long kocId);

    // Delete by campaign ID
    @Modifying
    @Query("DELETE FROM AffiliateLink al WHERE al.campaign.id = :campaignId")
    void deleteByCampaignId(@Param("campaignId") Long campaignId);
}
