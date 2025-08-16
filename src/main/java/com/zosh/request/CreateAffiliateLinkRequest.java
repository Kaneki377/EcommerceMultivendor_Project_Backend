package com.zosh.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateAffiliateLinkRequest {

    @NotNull(message = "campaignId is required")
    private Long campaignId;       // bắt buộc
    private Long productId;        // nullable nếu tạo link cho toàn campaign

    @Size(max=512)
    private String targetUrl;
}
