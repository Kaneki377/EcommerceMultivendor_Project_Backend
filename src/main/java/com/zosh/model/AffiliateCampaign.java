package com.zosh.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "affiliate_campaign")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AffiliateCampaign {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String campaignCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shop_id", nullable = false)
    private Seller seller;

    private String name;

    private String description;

    @Column(name = "commission_percent")
    private Double commissionPercent;

    @Column(name = "create_at")
    private LocalDateTime createdAt;

    @Column(name = "start_at")
    private LocalDateTime startAt;

    @Column(name = "expried")
    private LocalDateTime expiredAt;
    private Boolean active;

    @OneToMany(mappedBy = "affiliateCampaign", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Product> products = new ArrayList<>();

    // Cascade delete cho AffiliateLink
    @OneToMany(mappedBy = "campaign", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<AffiliateLink> affiliateLinks = new ArrayList<>();

    // Cascade delete cho AffiliateCommission
    @OneToMany(mappedBy = "campaign", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<AffiliateCommission> commissions = new ArrayList<>();

    // Cascade delete cho AffiliateRegistration
    @OneToMany(mappedBy = "campaign", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<AffiliateRegistration> registrations = new ArrayList<>();

}
