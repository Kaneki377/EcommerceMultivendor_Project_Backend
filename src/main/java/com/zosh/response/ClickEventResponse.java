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
public class ClickEventResponse {
    private Long id;
    private Long linkId;
    private Long campaignId;
    private Long productId;
    private String maskedIp;
    private String userAgent;
    private String referrer;
    private String sessionId;
    private LocalDateTime createdAt;
}
