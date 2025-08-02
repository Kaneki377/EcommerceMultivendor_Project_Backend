package com.zosh.service;

import com.zosh.exceptions.SellerException;
import com.zosh.model.AffiliateCampaign;
import com.zosh.request.CreateAffiliateCampaignRequest;

import java.util.List;
import java.util.Map;

public interface AffiliateCampaignService {
    AffiliateCampaign createCampaign(Long sellerId, CreateAffiliateCampaignRequest request) throws SellerException;
    List<AffiliateCampaign> getCampaignsBySeller(String jwt) throws SellerException;
    AffiliateCampaign partialUpdate(Long campaignId, Long sellerId, Map<String, Object> updates) throws Exception;
    void deleteCampaign(Long campaignId, Long sellerId) throws Exception;
}
