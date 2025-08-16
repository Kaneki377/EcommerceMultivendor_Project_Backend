package com.zosh.model;


import com.zosh.domain.PayoutStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(indexes = {
        @Index(name="idx_payout_koc", columnList="koc_id"),
        @Index(name="idx_payout_campaign", columnList="campaign_id"),
        @Index(name="idx_payout_status", columnList="status")
})
public class Payout {
    @Id
    @GeneratedValue
    Long id;

    @ManyToOne(fetch=FetchType.LAZY)
    Koc koc;

    @ManyToOne(fetch=FetchType.LAZY)
    AffiliateCampaign campaign;

    private LocalDateTime closedAt;      // thời điểm campaign đóng

    @Column(precision=18, scale=2)
    private BigDecimal grossAmount;

    @Column(precision=18, scale=2)
    private BigDecimal refundAdjustment;

    @Column(precision=18, scale=2)
    private BigDecimal netAmount;

    @Enumerated(EnumType.STRING)
    private PayoutStatus status = PayoutStatus.CREATED;

    private String method;               // BANK/...

    private String externalRef;          // mã giao dịch chi trả

    private LocalDateTime createdAt;

    private LocalDateTime paidAt;


}
