package com.zosh.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "order_attribution",
        indexes = {
                @Index(name = "idx_attr_order", columnList = "order_id"),
                @Index(name = "idx_attr_link", columnList = "affiliate_link_id")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderAttribution {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Gán theo toàn đơn
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    // Hoặc gán theo từng item (nullable)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_item_id")
    private OrderItem orderItem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "affiliate_link_id", nullable = false)
    private AffiliateLink affiliateLink;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "koc_id", nullable = false)
    private Koc koc;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "campaign_id", nullable = false)
    private AffiliateCampaign campaign;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    private LocalDateTime clickAt;
    private LocalDateTime attributedAt;

    @Enumerated(EnumType.STRING)
    private AttributionStatus status = AttributionStatus.PENDING;

    public enum AttributionStatus {
        PENDING, CONFIRMED, REVERSED
    }
}
