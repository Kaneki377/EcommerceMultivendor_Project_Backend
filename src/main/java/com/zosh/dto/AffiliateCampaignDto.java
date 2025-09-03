package com.zosh.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AffiliateCampaignDto {
    private Long id;
    private String campaignCode;
    private String name;
    private String description;
    private Double commissionPercent;
    private LocalDateTime createdAt;
    private LocalDateTime expiredAt;
    private Boolean active;
}
