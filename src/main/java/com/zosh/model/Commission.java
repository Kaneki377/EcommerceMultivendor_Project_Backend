package com.zosh.model;

import com.zosh.domain.CommissionStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "commission",
        indexes = {
                @Index(name = "idx_commission_koc", columnList = "koc_id"),
                @Index(name = "idx_commission_order", columnList = "order_id")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Commission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Người nhận hoa hồng
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "koc_id", nullable = false)
    private Koc koc;

    // Chiến dịch
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "campaign_id", nullable = false)
    private AffiliateCampaign campaign;

    // Đơn & (tuỳ chọn) Item
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_item_id")
    private OrderItem orderItem;

    // (tuỳ chọn) tham chiếu gán công
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attribution_id")
    private OrderAttribution attribution;

    // Tỷ lệ và số tiền
    @Column(name = "commission_rate", precision = 9, scale = 6)
    private BigDecimal commissionRate;     // ví dụ 0.02

    @Column(name = "base_amount", precision = 18, scale = 2)
    private BigDecimal baseAmount;         // netPaidAmount

    @Column(name = "commission_amount", precision = 18, scale = 2)
    private BigDecimal commissionAmount;   // baseAmount * commissionRate

    @Enumerated(EnumType.STRING)
    private CommissionStatus status = CommissionStatus.PENDING;

    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime holdUntil; // nếu có thời gian hold
}
