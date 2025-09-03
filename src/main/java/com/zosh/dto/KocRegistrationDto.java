package com.zosh.dto;

import com.zosh.domain.RegistrationStatus;
import lombok.Data;

import java.time.LocalDateTime;

// DTO for Seller view of KOC registrations
@Data
public class KocRegistrationDto {
    private Long id;
    private KocInfo koc;
    private CampaignInfo campaign;
    private LocalDateTime registeredAt;
    private RegistrationStatus status;

    @Data
    public static class KocInfo {
        private Long id;
        private String kocCode;
        private CustomerInfo customer;
    }

    @Data
    public static class CustomerInfo {
        private String fullName;
        private AccountInfo account;
    }

    @Data
    public static class AccountInfo {
        private String username;
    }

    @Data
    public static class CampaignInfo {
        private Long id;
        private String campaignCode;
        private String name;
        private Double commissionPercent;
        private Integer productCount; // Số lượng sản phẩm trong campaign
    }
}
