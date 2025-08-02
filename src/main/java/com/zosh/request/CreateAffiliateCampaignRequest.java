package com.zosh.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class CreateAffiliateCampaignRequest {

    @NotBlank(message = "Campaign name cannot be blank")
    private String name;

    private String description;

    @NotNull(message = "Commission percentage is mandatory")
    @DecimalMin(value = "0.0", inclusive = false, message = "Commission percentage must be greater than 0")
    @DecimalMax(value = "100.0", message = "Commission percentage cannot exceed 100")
    private Double commissionPercent;

    @NotNull(message = "Expiration time is required")
    @Future(message = "Expiration date must be in the future")
    private LocalDateTime expiredAt;

    @NotEmpty(message = "Must select at least 1 product")
    private List<Long> productIds; // danh sách ID sản phẩm được gắn vào campaign

    // ✅ Custom validate: expired phải sau thời điểm hiện tại (tức là sau thời gian tạo campaign)
    @AssertTrue(message = "Expiration time must be after the current time")
    public boolean isExpiredValid() {
        return expiredAt != null && expiredAt.isAfter(LocalDateTime.now());
    }
}
