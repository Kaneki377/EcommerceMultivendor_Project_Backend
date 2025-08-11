package com.zosh.service;

import com.zosh.model.AffiliateLink;
import com.zosh.response.AffiliateLinkResponse;
import com.zosh.response.ClickStatsResponse;

import java.time.LocalDateTime;
import java.util.List;

public interface AffiliateLinkService {
    AffiliateLinkResponse createAffiliateLink(Long kocId, Long campaignId, Long productId, String targetUrl);
    List<AffiliateLinkResponse> getLinksByKoc(Long kocId);
    AffiliateLink getByCode(String code);
    ClickStatsResponse getLinkStats(Long linkId, LocalDateTime from, LocalDateTime to);
}
