package com.zosh.service;

import com.zosh.exceptions.SellerException;
import com.zosh.model.AffiliateCampaign;
import com.zosh.request.CreateAffiliateCampaignRequest;

public interface AffiliateCampaignService {
    AffiliateCampaign createCampaign(Long sellerId, CreateAffiliateCampaignRequest request) throws SellerException;
}
