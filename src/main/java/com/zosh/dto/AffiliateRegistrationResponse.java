package com.zosh.dto;

import com.zosh.domain.RegistrationStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AffiliateRegistrationResponse {

    private Long id;
    private Long campaignId;
    private String campaignTitle;
    private String campaignDescription; // optional
    private Double commissionPercent; // optional
    private LocalDateTime startedAt; // campaign.createdAt
    private LocalDateTime expiredAt; // campaign.expiredAt
    private LocalDateTime registeredAt;
    private RegistrationStatus status;

    public AffiliateRegistrationResponse(Long id, Long campaignId, String campaignTitle,
            LocalDateTime registeredAt, RegistrationStatus status) {
        this.id = id;
        this.campaignId = campaignId;
        this.campaignTitle = campaignTitle;
        this.registeredAt = registeredAt;
        this.status = status;
    }

    // Getters & setters nếu bạn không dùng Lombok
}
