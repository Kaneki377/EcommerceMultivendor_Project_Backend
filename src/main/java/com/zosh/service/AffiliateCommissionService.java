package com.zosh.service;

import com.zosh.model.AffiliateCommission;
import com.zosh.model.AffiliateCommission.CommissionStatus;
import com.zosh.model.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface AffiliateCommissionService {

    /**
     * Tạo commission cho các OrderItem có affiliate link
     */
    List<AffiliateCommission> createCommissionsForOrder(Order order);

    /**
     * Cập nhật status commission khi order status thay đổi
     */
    void updateCommissionStatus(Long orderId, CommissionStatus status);

    /**
     * Lấy danh sách commission của KOC
     */
    List<AffiliateCommission> getKocCommissions(Long kocId);

    /**
     * Lấy commission với phân trang
     */
    Page<AffiliateCommission> getKocCommissions(Long kocId, Pageable pageable);

    /**
     * Tổng commission theo status của KOC
     */
    Long getTotalCommissionByStatus(Long kocId, CommissionStatus status);

    /**
     * Dashboard data cho KOC
     */
    Map<String, Object> getKocDashboardData(Long kocId);

    /**
     * Commission theo thời gian
     */
    List<AffiliateCommission> getCommissionsByDateRange(Long kocId, LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Thống kê commission theo tháng
     */
    List<Map<String, Object>> getMonthlyCommissionStats(Long kocId);

    /**
     * Đánh dấu commission đã được trả
     */
    void markCommissionAsPaid(List<Long> commissionIds);

    /**
     * Lấy tất cả commission confirmed (chưa trả) để admin xử lý
     */
    List<AffiliateCommission> getConfirmedCommissions();
}
