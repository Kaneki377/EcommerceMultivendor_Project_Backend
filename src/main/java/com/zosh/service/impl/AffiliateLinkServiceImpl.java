package com.zosh.service.impl;

import com.zosh.domain.RegistrationStatus;
import com.zosh.exceptions.KocException;
import com.zosh.model.AffiliateCampaign;
import com.zosh.model.AffiliateLink;
import com.zosh.model.Koc;
import com.zosh.model.Product;
import com.zosh.repository.*;
import com.zosh.response.AffiliateLinkResponse;
import com.zosh.service.AffiliateCampaignService;
import com.zosh.service.AffiliateLinkService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AffiliateLinkServiceImpl implements AffiliateLinkService {

    private final AffiliateLinkRepository affiliateLinkRepository;
    private final KocRepository kocRepository;
    private final AffiliateCampaignRepository campaignRepository;
    private final ProductRepository productRepository;
    private final AffiliateRegistrationRepository registrationRepository;
    private final AffiliateCampaignService affiliateCampaignService;


    private static final char[] BASE62 = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz".toCharArray();
    private static final SecureRandom RNG = new java.security.SecureRandom();

    @Override
    @Transactional
    public AffiliateLinkResponse createAffiliateLink(Long kocId, Long campaignId, Long productId, String targetUrl) {
        Koc koc = kocRepository.findById(kocId)
                .orElseThrow(() -> new KocException("KOC not found"));

        AffiliateCampaign campaign = campaignRepository.findById(campaignId)
                .orElseThrow(() -> new RuntimeException("Campaign not found"));

        // Campaign phải đang/được phép hoạt động để tạo link
        if (!affiliateCampaignService.isActive(campaign)) {
            throw new RuntimeException("Campaign is not active");
        }

        boolean approved = registrationRepository
                .existsByKoc_IdAndCampaign_IdAndStatus(kocId, campaignId, RegistrationStatus.APPROVED);
        if (!approved) throw new RuntimeException("KOC is not approved for this campaign");

        Product product = null;
        if (productId != null) {
            product = productRepository.findById(productId)
                    .orElseThrow(() -> new RuntimeException("Product not found"));

            if (product.getAffiliateCampaign() == null ||
                    !product.getAffiliateCampaign().getId().equals(campaignId)) {
                throw new RuntimeException("Product is not in this campaign");
            }
            // không tạo trùng link theo bộ (koc, campaign, product)
            affiliateLinkRepository.findByKoc_IdAndCampaign_IdAndProduct_Id(kocId, campaignId, productId)
                    .ifPresent(x -> { throw new RuntimeException("Link already exists for this KOC/campaign/product"); });
        } else {
            // link toàn campaign: (koc, campaign, product=null) duy nhất
            affiliateLinkRepository.findByKoc_IdAndCampaign_IdAndProduct_IsNull(kocId, campaignId)
                    .ifPresent(x -> { throw new RuntimeException("Campaign-wide link already exists for this KOC"); });
        }

        // Build targetUrl theo yêu cầu
        String resolvedTarget = resolveTargetUrl(targetUrl, campaign, product, koc);

        // Tạo link
        AffiliateLink link = new AffiliateLink();
        link.setKoc(koc);
        link.setCampaign(campaign);
        link.setProduct(product);
        link.setTargetUrl(resolvedTarget);
        link.setCreatedAt(LocalDateTime.now());
        link.setTotalClick(0);

        // shortToken (ULID/Random Base62)
        String token = generateShortToken();
        // đảm bảo duy nhất
        while (affiliateLinkRepository.findByShortToken(token).isPresent()) {
            token = generateShortToken();
        }
        link.setShortToken(token);

        // generatedUrl dùng shortToken
        link.setGeneratedUrl("/r/" + token);

        link = affiliateLinkRepository.save(link);

        return AffiliateLinkResponse.builder()
                .id(link.getId())
                .campaignId(campaignId)
                .productId(productId)
                .targetUrl(resolvedTarget)
                .generatedUrl(link.getGeneratedUrl())
                .createdAt(link.getCreatedAt())
                .totalClicks((long) link.getTotalClick())
                .build();
    }

    @Override
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public List<AffiliateLinkResponse> getLinksByKoc(Long kocId) {
        return affiliateLinkRepository.findByKoc_Id(kocId)
                .stream()
                .map(l -> AffiliateLinkResponse.builder()
                        .id(l.getId())
                        .campaignId(l.getCampaign().getId())
                        .productId(l.getProduct() != null ? l.getProduct().getId() : null)
                        .targetUrl(l.getTargetUrl())
                        .generatedUrl(l.getGeneratedUrl())
                        .createdAt(l.getCreatedAt())
                        .totalClicks((long) l.getTotalClick())
                        .build())
                .collect(Collectors.toList());
    }

    // Redirect dùng token
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public AffiliateLink getByShortToken(String token) {
        return affiliateLinkRepository.findByShortToken(token)
                .orElseThrow(() -> new RuntimeException("Affiliate link not found"));
    }

    // ================= helpers =================

    private String resolveTargetUrl(String targetUrl, AffiliateCampaign campaign, Product product, Koc koc) {
        if (StringUtils.hasText(targetUrl)) return targetUrl;

        // Theo yêu cầu: /products/{productId}?koc={kocCode}&cmp={campaignCode}
        if (product != null) {
            return "/products/" + product.getId()
                    + "?koc=" + koc.getKocCode()
                    + "&cmp=" + campaign.getCampaignCode();
        }
        // Nếu link toàn campaign: có thể điều hướng trang campaign
        return "/campaigns/" + campaign.getId()
                + "?koc=" + koc.getKocCode()
                + "&cmp=" + campaign.getCampaignCode();
    }


    private String generateShortToken() {
        int len = 12;
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            sb.append(BASE62[RNG.nextInt(BASE62.length)]);
        }
        return sb.toString();
    }


}
