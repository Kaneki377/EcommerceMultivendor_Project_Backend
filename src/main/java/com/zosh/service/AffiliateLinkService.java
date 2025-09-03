package com.zosh.service;

import com.zosh.dto.AffiliateLinkDto;
import com.zosh.model.AffiliateLink;
import com.zosh.model.AffiliateCampaign;
import com.zosh.model.Koc;
import com.zosh.model.Product;

import java.util.List;

public interface AffiliateLinkService {

    /**
     * Tự động tạo affiliate link cho KOC khi được approve vào campaign
     * 
     * @param koc      KOC được approve
     * @param campaign Campaign được approve
     * @return AffiliateLink đã tạo
     */
    AffiliateLink createCampaignLink(Koc koc, AffiliateCampaign campaign);

    /**
     * Tạo affiliate link cho product cụ thể
     * 
     * @param koc      KOC
     * @param product  Product cụ thể
     * @param campaign Campaign chứa product
     * @return AffiliateLink đã tạo
     */
    AffiliateLink createProductLink(Koc koc, Product product, AffiliateCampaign campaign);

    /**
     * Lấy tất cả link của KOC
     *
     * @param jwt JWT token của KOC
     * @return List<AffiliateLink>
     */
    List<AffiliateLink> getMyLinks(String jwt);

    /**
     * Lấy tất cả affiliate links của KOC dưới dạng DTO
     * 
     * @param jwt JWT token của KOC
     * @return List<AffiliateLinkDto>
     */
    List<AffiliateLinkDto> getKocAffiliateLinks(String jwt);

    /**
     * Lấy link của KOC trong campaign cụ thể
     * 
     * @param jwt        JWT token của KOC
     * @param campaignId ID campaign
     * @return List<AffiliateLink>
     */
    List<AffiliateLink> getMyLinksInCampaign(String jwt, Long campaignId);

    /**
     * Redirect và tăng click count
     * 
     * @param shortToken Token ngắn của link
     * @return URL để redirect
     */
    String redirectAndCount(String shortToken);

    /**
     * Tạo short token unique
     * 
     * @return String token ngắn
     */
    String generateShortToken();

    /**
     * Tạo target URL cho campaign
     * 
     * @param campaign Campaign
     * @param koc      KOC
     * @return Target URL
     */
    String buildCampaignTargetUrl(AffiliateCampaign campaign, Koc koc);

    /**
     * Tạo target URL cho product
     * 
     * @param product  Product
     * @param koc      KOC
     * @param campaign Campaign
     * @return Target URL
     */
    String buildProductTargetUrl(Product product, Koc koc, AffiliateCampaign campaign);
}
