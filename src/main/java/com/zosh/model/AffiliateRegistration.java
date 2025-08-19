package com.zosh.model;

import com.zosh.domain.RegistrationStatus;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "affiliate_registration",
        uniqueConstraints = @UniqueConstraint(
                name="uk_affreg_koc_campaign",
                columnNames = {"koc_id","campaign_id"}
        ),
        indexes = {
                @Index(name="idx_affreg_koc", columnList="koc_id"),
                @Index(name="idx_affreg_campaign", columnList="campaign_id")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AffiliateRegistration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "koc_id", nullable = false)
    private Koc koc;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "campaign_id", nullable = false)
    private AffiliateCampaign campaign;

    @Column(name = "registered_at")
    private LocalDateTime registeredAt;

    @Enumerated(EnumType.STRING)
    private RegistrationStatus status;

}
