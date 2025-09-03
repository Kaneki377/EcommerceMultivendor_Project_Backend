package com.zosh.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "affiliate_commission")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AffiliateCommission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "koc_id", nullable = false)
    private Koc koc;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_item_id", nullable = false)
    private OrderItem orderItem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "affiliate_link_id", nullable = false)
    private AffiliateLink affiliateLink;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "campaign_id", nullable = false)
    private AffiliateCampaign campaign;

    // Số tiền commission (đã tính theo %)
    private Long commissionAmount;

    // % commission tại thời điểm tạo order
    private Double commissionPercent;

    // Tổng giá trị order được tính commission
    private Long orderValue;

    @Enumerated(EnumType.STRING)
    private CommissionStatus status = CommissionStatus.PENDING;

    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime paidAt;

    public enum CommissionStatus {
        PENDING, // Chờ order hoàn thành
        CONFIRMED, // Order đã DELIVERED, commission được xác nhận
        PAID, // Đã trả commission cho KOC
        CANCELLED // Order bị hủy, commission không được trả
    }
}
