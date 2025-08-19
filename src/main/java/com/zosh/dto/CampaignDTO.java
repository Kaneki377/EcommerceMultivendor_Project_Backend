package com.zosh.dto;


import com.zosh.model.AffiliateCampaign;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CampaignDTO {

    private Long id;
    private String campaignCode;
    private String name;
    private String description;
    private Double commissionPercent;
    private Boolean active;
    private LocalDateTime expiredAt;
    private Long sellerId;
    private String sellerName;
    private String myStatus; // APPROVED/PENDING/REJECTED/null

    public static CampaignDTO from(AffiliateCampaign c, Enum<?> myStatus) {
        return CampaignDTO.builder()
                .id(c.getId())
                .campaignCode(c.getCampaignCode())
                .name(c.getName())
                .description(c.getDescription())
                .commissionPercent(c.getCommissionPercent())
                .active(c.getActive())
                .expiredAt(c.getExpiredAt())
                .sellerId(c.getSeller().getId())
                .sellerName(c.getSeller().getSellerName())
                .myStatus(myStatus == null ? null : myStatus.name())
                .build();
    }
}
