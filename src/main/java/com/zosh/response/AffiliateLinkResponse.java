package com.zosh.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AffiliateLinkResponse {

    private Long id;
    private Long campaignId;
    private Long productId;
    private String code;
    private String targetUrl;
    private String generatedUrl;
    private LocalDateTime createdAt;

    // số click tổng (tuỳ chọn)
    private Long totalClicks;
}
