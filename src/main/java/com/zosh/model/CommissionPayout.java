package com.zosh.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "commission_payout")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CommissionPayout {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "koc_id", nullable = false)
    private Koc koc;

    private Long amount;

    @Enumerated(EnumType.STRING)
    private PayoutStatus status;

    @Column(name = "payout_date")
    private LocalDateTime payoutDate;

    private String note;

    public enum PayoutStatus {
        PENDING,
        PAID,
        FAILED
    }
}
