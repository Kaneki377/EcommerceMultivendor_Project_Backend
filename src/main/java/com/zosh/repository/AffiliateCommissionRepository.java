package com.zosh.repository;

import com.zosh.model.AffiliateCommission;
import com.zosh.model.AffiliateCommission.CommissionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AffiliateCommissionRepository extends JpaRepository<AffiliateCommission, Long> {

    // Tìm commission theo KOC
    List<AffiliateCommission> findByKoc_Id(Long kocId);

    // Tìm commission theo KOC và status
    List<AffiliateCommission> findByKoc_IdAndStatus(Long kocId, CommissionStatus status);


    //kiểm tra tồn tại theo orderItem
    boolean existsByOrderItem_Id(Long orderItemId);

    List<AffiliateCommission> findByStatus(AffiliateCommission.CommissionStatus status);

    // Tìm commission theo order (thông qua orderItem)
    @Query("SELECT ac FROM AffiliateCommission ac WHERE ac.orderItem.order.id = :orderId")
    List<AffiliateCommission> findByOrderId(@Param("orderId") Long orderId);

    // Tổng commission đã được xác nhận của KOC
    @Query("SELECT SUM(ac.commissionAmount) FROM AffiliateCommission ac WHERE ac.koc.id = :kocId AND ac.status = :status")
    Long getTotalCommissionByKocAndStatus(@Param("kocId") Long kocId, @Param("status") CommissionStatus status);

    // Tổng commission pending của KOC
    @Query("SELECT SUM(ac.commissionAmount) FROM AffiliateCommission ac WHERE ac.koc.id = :kocId AND ac.status = 'PENDING'")
    Long getPendingCommissionByKoc(@Param("kocId") Long kocId);

    // Delete by campaign ID
    @Modifying
    @Query("DELETE FROM AffiliateCommission ac WHERE ac.campaign.id = :campaignId")
    void deleteByCampaignId(@Param("campaignId") Long campaignId);

    // Tổng commission confirmed của KOC (chưa trả)
    @Query("SELECT SUM(ac.commissionAmount) FROM AffiliateCommission ac WHERE ac.koc.id = :kocId AND ac.status = 'CONFIRMED'")
    Long getConfirmedCommissionByKoc(@Param("kocId") Long kocId);

    // Tổng commission đã trả của KOC
    @Query("SELECT SUM(ac.commissionAmount) FROM AffiliateCommission ac WHERE ac.koc.id = :kocId AND ac.status = 'PAID'")
    Long getPaidCommissionByKoc(@Param("kocId") Long kocId);

    // Commission theo thời gian
    @Query("SELECT ac FROM AffiliateCommission ac WHERE ac.koc.id = :kocId AND ac.createdAt BETWEEN :startDate AND :endDate")
    List<AffiliateCommission> findByKocAndDateRange(@Param("kocId") Long kocId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    // Phân trang commission của KOC
    Page<AffiliateCommission> findByKoc_Id(Long kocId, Pageable pageable);

    // Commission theo campaign
    List<AffiliateCommission> findByCampaign_Id(Long campaignId);

    // Thống kê commission theo tháng cho KOC
    @Query("""
    SELECT YEAR(ac.createdAt), MONTH(ac.createdAt), SUM(ac.commissionAmount)
    FROM AffiliateCommission ac
    WHERE ac.koc.id = :kocId AND ac.status = :status
    GROUP BY YEAR(ac.createdAt), MONTH(ac.createdAt)
    ORDER BY MAX(ac.createdAt) DESC
            """)

    List<Object[]> getMonthlyCommissionStats(@Param("kocId") Long kocId, @Param("status") CommissionStatus status);


}
