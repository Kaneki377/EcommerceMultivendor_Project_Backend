package com.zosh.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = false)
    private Seller seller;

    private String name;

    private String description;

    @Column(name = "commission_percent")
    private Double commissionPercent;

    @Column(name = "create_at")
    private LocalDateTime createAt;

    private Boolean active;
}
