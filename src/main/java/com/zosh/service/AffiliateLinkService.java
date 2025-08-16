package com.zosh.service;

import com.zosh.model.AffiliateLink;
import com.zosh.response.AffiliateLinkResponse;

import java.util.List;

public interface AffiliateLinkService {
    AffiliateLinkResponse createAffiliateLink(Long kocId, Long campaignId, Long productId, String targetUrl);
    List<AffiliateLinkResponse> getLinksByKoc(Long kocId);
    public AffiliateLink getByShortToken(String token);
}
