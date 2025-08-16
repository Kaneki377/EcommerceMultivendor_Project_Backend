package com.zosh.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Table(indexes = {
        @Index(name = "idx_oi_order", columnList = "order_id"),
        @Index(name = "idx_oi_product", columnList = "product_id"),
        @Index(name = "idx_oi_afflink", columnList = "affiliate_link_id")
})
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne
    private Order order;

    @ManyToOne
    private Product product;

    private String size;

    private int quantity;

    private Integer mrpPrice;

    private Integer sellingPrice;

    private Long customerId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "affiliate_link_id")
    private AffiliateLink affiliateLink;

    @Enumerated(EnumType.STRING)
    @Column(name = "commission_status", nullable = false)
    private CommissionStatus commissionStatus = CommissionStatus.PENDING;

    @Column(name = "commission_amount", precision = 18, scale = 2, nullable = false)
    private BigDecimal commissionAmount = BigDecimal.ZERO; // ✅ line total * %snapshot

    @Column(name = "attributed_at")
    private LocalDateTime attributedAt;

    public enum CommissionStatus {
        PENDING,
        PAYABLE,
        PAID,
        REVERSED }
}
