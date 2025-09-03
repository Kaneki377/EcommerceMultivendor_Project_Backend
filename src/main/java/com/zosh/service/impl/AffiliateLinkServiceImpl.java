package com.zosh.service.impl;

import com.zosh.config.JwtProvider;
import com.zosh.dto.AffiliateLinkDto;
import com.zosh.exceptions.KocException;
import com.zosh.model.*;
import com.zosh.repository.AffiliateLinkRepository;
import com.zosh.repository.KocRepository;
import com.zosh.service.AffiliateLinkService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class AffiliateLinkServiceImpl implements AffiliateLinkService {

    private final AffiliateLinkRepository linkRepository;
    private final KocRepository kocRepository;
    private final JwtProvider jwtProvider;

    private static final String BASE_URL = "http://localhost:5173"; // Frontend URL
    private static final String REDIRECT_PATH = "/r/"; // Path cho redirect
    private static final String BASE_URL_SHORT = "http://localhost:5454";
    @Override
    public AffiliateLink createCampaignLink(Koc koc, AffiliateCampaign campaign) {
        // Kiểm tra đã có link cho campaign này chưa
        if (linkRepository.existsByKocAndCampaign(koc, campaign)) {
            return linkRepository.findByKoc_IdAndCampaign_IdAndProductIsNull(koc.getId(), campaign.getId())
                    .orElseThrow(() -> new RuntimeException("Link exists but not found"));
        }

        AffiliateLink link = new AffiliateLink();
        link.setKoc(koc);
        link.setCampaign(campaign);
        link.setProduct(null); // Link cho toàn campaign
        link.setShortToken(generateShortToken());
        link.setTargetUrl(buildCampaignTargetUrl(campaign, koc));
        link.setGeneratedUrl(BASE_URL_SHORT + REDIRECT_PATH + link.getShortToken());
        link.setTotalClick(0);
        link.setCreatedAt(LocalDateTime.now());

        return linkRepository.save(link);
    }

    @Override
    public AffiliateLink createProductLink(Koc koc, Product product, AffiliateCampaign campaign) {
        // Kiểm tra đã có link cho product này chưa
        if (linkRepository.existsByKocAndProduct(koc, product)) {
            return linkRepository.findByKoc_IdAndProduct_Id(koc.getId(), product.getId())
                    .orElseThrow(() -> new RuntimeException("Link exists but not found"));
        }

        AffiliateLink link = new AffiliateLink();
        link.setKoc(koc);
        link.setCampaign(campaign);
        link.setProduct(product);
        link.setShortToken(generateShortToken());
        link.setTargetUrl(buildProductTargetUrl(product, koc, campaign));
        link.setGeneratedUrl(BASE_URL_SHORT + REDIRECT_PATH + link.getShortToken());
        link.setTotalClick(0);
        link.setCreatedAt(LocalDateTime.now());

        return linkRepository.save(link);
    }

    @Override
    public List<AffiliateLink> getMyLinks(String jwt) {
        String username = jwtProvider.getUsernameFromJwtToken(jwt);
        Koc koc = kocRepository.findByCustomer_Account_Username(username)
                .orElseThrow(() -> new KocException("KOC not found"));

        return linkRepository.findByKoc_Id(koc.getId());
    }

    @Override
    public List<AffiliateLinkDto> getKocAffiliateLinks(String jwt) {
        String username = jwtProvider.getUsernameFromJwtToken(jwt);
        Koc koc = kocRepository.findByCustomer_Account_Username(username)
                .orElseThrow(() -> new KocException("KOC not found"));

        List<AffiliateLink> links = linkRepository.findByKoc_Id(koc.getId());

        return links.stream().map(link -> {
            AffiliateLinkDto dto = new AffiliateLinkDto();
            dto.setId(link.getId());
            dto.setShortToken(link.getShortToken());
            dto.setGeneratedUrl(link.getGeneratedUrl());
            dto.setTargetUrl(link.getTargetUrl());
            dto.setTotalClick(link.getTotalClick());
            dto.setCreatedAt(link.getCreatedAt());

            // Campaign info
            dto.setCampaignId(link.getCampaign().getId());
            dto.setCampaignCode(link.getCampaign().getCampaignCode());
            dto.setCampaignName(link.getCampaign().getName());
            dto.setCommissionPercent(link.getCampaign().getCommissionPercent());

            // Product info (if exists)
            if (link.getProduct() != null) {
                dto.setProductId(link.getProduct().getId());
                dto.setProductTitle(link.getProduct().getTitle());
                dto.setProductPrice((double) link.getProduct().getSellingPrice());
                if (!link.getProduct().getImages().isEmpty()) {
                    dto.setProductImage(link.getProduct().getImages().get(0));
                }
            }

            // KOC info
            dto.setKocCode(link.getKoc().getKocCode());
            dto.setKocName(link.getKoc().getCustomer().getFullName());

            return dto;
        }).toList();
    }

    @Override
    public List<AffiliateLink> getMyLinksInCampaign(String jwt, Long campaignId) {
        String username = jwtProvider.getUsernameFromJwtToken(jwt);
        Koc koc = kocRepository.findByCustomer_Account_Username(username)
                .orElseThrow(() -> new KocException("KOC not found"));

        return linkRepository.findByKoc_IdAndCampaign_Id(koc.getId(), campaignId);
    }

    @Override
    public String redirectAndCount(String shortToken) {
        AffiliateLink link = linkRepository.findByShortToken(shortToken)
                .orElseThrow(() -> new RuntimeException("Link not found"));

        // Tăng click count
        link.setTotalClick(link.getTotalClick() + 1);
        linkRepository.save(link);

        return link.getTargetUrl();
    }

    @Override
    public String generateShortToken() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new Random();
        StringBuilder token;

        do {
            token = new StringBuilder();
            for (int i = 0; i < 8; i++) {
                token.append(chars.charAt(random.nextInt(chars.length())));
            }
        } while (linkRepository.findByShortToken(token.toString()).isPresent());

        return token.toString();
    }

    @Override
    public String buildCampaignTargetUrl(AffiliateCampaign campaign, Koc koc) {
        // URL dạng: /campaigns/{campaignCode}?koc={kocCode}
        return BASE_URL + "/campaigns/" + campaign.getCampaignCode() + "?koc=" + koc.getKocCode();
    }

    @Override
    public String buildProductTargetUrl(Product product, Koc koc, AffiliateCampaign campaign) {
        // URL dạng:
        // /product-details/{categoryId}/{name}/{productId}?koc={kocCode}&campaign={campaignCode}
        // Frontend route pattern: /product-details/:categoryId/:name/:productId
        // Tạo name từ title (URL-friendly)
        String name = product.getTitle()
                .toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "") // Remove special characters
                .replaceAll("\\s+", "-") // Replace spaces with hyphens
                .replaceAll("-+", "-") // Replace multiple hyphens with single
                .replaceAll("^-|-$", ""); // Remove leading/trailing hyphens

        return BASE_URL + "/product-details/" +
                product.getCategory().getId() + "/" +
                name + "/" +
                product.getId() +
                "?koc=" + koc.getKocCode() +
                "&campaign=" + campaign.getCampaignCode();
    }
}
