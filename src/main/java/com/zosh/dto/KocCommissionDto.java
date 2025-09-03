package com.zosh.dto;

import com.zosh.model.AffiliateCommission.CommissionStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class KocCommissionDto {
    private Long id;
    private String orderId;
    private Long orderValue;
    private Double commissionPercent;
    private Long commissionAmount;
    private CommissionStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime paidAt;
    
    // Campaign info
    private String campaignName;
    private String campaignCode;
    
    // Product info (if applicable)
    private String productTitle;
    private String productImage;
}
