package com.zosh.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "click_event", indexes = {
        @Index(name = "idx_click_link", columnList = "affiliate_link_id"),
        @Index(name = "idx_click_campaign", columnList = "campaign_id"),
        @Index(name = "idx_click_koc", columnList = "koc_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ClickEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Link được click
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "affiliate_link_id", nullable = false)
    private AffiliateLink affiliateLink;

    // Redundant để query/report nhanh
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "koc_id", nullable = false)
    private Koc koc;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "campaign_id", nullable = false)
    private AffiliateCampaign campaign;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    @Column(length = 64)
    private String ip;

    @Column(name = "user_agent", length = 512)
    private String userAgent;

    @Column(length = 512)
    private String referrer;

    @Column(name = "session_id", length = 64)
    private String sessionId;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
