package com.zosh.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(uniqueConstraints = {
                @UniqueConstraint(name = "uk_link_koc_cmp_prod", columnNames = { "koc_id", "campaign_id",
                                "product_id" })
}, indexes = {
                @Index(name = "idx_link_campaign", columnList = "campaign_id"),
                @Index(name = "idx_link_product", columnList = "product_id"),
                @Index(name = "idx_link_koc", columnList = "koc_id")
})
public class AffiliateLink {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        // Link thuộc chiến dịch
        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "campaign_id", nullable = false)
        private AffiliateCampaign campaign;

        // Có thể null nếu link cho toàn campaign
        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "product_id")
        private Product product;

        // Chủ sở hữu link
        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "koc_id", nullable = false)
        private Koc koc;

        @Column(name = "short_token", length = 22, nullable = false, unique = true) // Base62 của id hoặc ULID
        private String shortToken;

        private int totalClick;

        // URL gốc (nội bộ hay ngoài đều được)
        @Column(name = "target_url", length = 512)
        private String targetUrl;

        // URL công khai mà KOC share, ví dụ: /r/{code}
        @Column(name = "generated_url", length = 512)
        private String generatedUrl;

        @Column(name = "created_at")
        private LocalDateTime createdAt;
}