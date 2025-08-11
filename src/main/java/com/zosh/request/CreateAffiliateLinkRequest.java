package com.zosh.request;

import lombok.Data;

@Data
public class CreateAffiliateLinkRequest {

    private Long campaignId;       // bắt buộc
    private Long productId;        // nullable nếu tạo link cho toàn campaign
    private String targetUrl;
}
