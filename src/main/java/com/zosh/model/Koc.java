package com.zosh.model;

import com.zosh.domain.AccountStatus;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "koc")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Koc {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "koc_id", unique = true, nullable = false)
    private String kocId;

    @Enumerated(EnumType.STRING)
    private AccountStatus accountStatus = AccountStatus.PENDING_VERIFICATION;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Column(name = "facebook_link", length = 255)
    private String facebookLink;

    @Column(name = "instagram_link", length = 255)
    private String instagramLink;

    @Column(name = "tiktok_link", length = 255)
    private String tiktokLink;

    @Column(name = "youtube_link", length = 255)
    private String youtubeLink;

    @Column(name = "joined_at")
    private LocalDateTime joinedAt;
}
