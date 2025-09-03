package com.zosh.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AffiliateLinkDto {
    private Long id;
    private String shortToken;
    private String generatedUrl;
    private String targetUrl;
    private int totalClick;
    private LocalDateTime createdAt;
    
    // Campaign info
    private Long campaignId;
    private String campaignCode;
    private String campaignName;
    private Double commissionPercent;
    
    // Product info (nullable)
    private Long productId;
    private String productTitle;
    private String productImage;
    private Double productPrice;
    
    // KOC info
    private String kocCode;
    private String kocName;
}
