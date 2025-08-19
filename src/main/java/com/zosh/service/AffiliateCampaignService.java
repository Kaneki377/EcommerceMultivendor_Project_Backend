package com.zosh.service;

import com.zosh.dto.CampaignDTO;
import com.zosh.exceptions.SellerException;
import com.zosh.model.AffiliateCampaign;
import com.zosh.request.CreateAffiliateCampaignRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

public interface AffiliateCampaignService {
    AffiliateCampaign createCampaign(Long sellerId, CreateAffiliateCampaignRequest request) throws SellerException;
    List<AffiliateCampaign> getCampaignsBySeller(String jwt) throws SellerException;
    AffiliateCampaign partialUpdate(Long campaignId, Long sellerId, Map<String, Object> updates) throws Exception;
    void deleteCampaign(Long campaignId, Long sellerId) throws Exception;
    boolean isActive(AffiliateCampaign campaign);
    public Page<CampaignDTO> listActiveWithMyStatus(String jwt, Pageable pageable);
    public Page<AffiliateCampaign> listActiveForKocNotRegistered(String jwt, Pageable pageable);

}
