package com.zosh.controller;

import com.zosh.dto.AffiliateLinkDto;
import com.zosh.model.AffiliateLink;
import com.zosh.service.AffiliateLinkService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class AffiliateLinkController {

    private final AffiliateLinkService linkService;

    /**
     * KOC lấy tất cả link của mình
     */
    @GetMapping("/api/koc/my-links")
    @PreAuthorize("hasRole('KOC')")
    public ResponseEntity<List<AffiliateLinkDto>> getMyLinks(
            @RequestHeader("Authorization") String jwt) {

        List<AffiliateLinkDto> dtos = linkService.getKocAffiliateLinks(jwt);
        return ResponseEntity.ok(dtos);
    }

    /**
     * KOC lấy link trong campaign cụ thể
     */
    @GetMapping("/api/koc/my-links/campaign/{campaignId}")
    @PreAuthorize("hasRole('KOC')")
    public ResponseEntity<List<AffiliateLinkDto>> getMyLinksInCampaign(
            @PathVariable Long campaignId,
            @RequestHeader("Authorization") String jwt) {

        List<AffiliateLink> links = linkService.getMyLinksInCampaign(jwt, campaignId);
        List<AffiliateLinkDto> dtos = links.stream()
                .map(this::convertToDto)
                .toList();
        return ResponseEntity.ok(dtos);
    }

    /**
     * Public endpoint để redirect link rút gọn
     * URL: /r/{shortToken}
     */
    @GetMapping("/r/{shortToken}")
    public RedirectView redirectLink(@PathVariable String shortToken) {
        String targetUrl = linkService.redirectAndCount(shortToken);
        return new RedirectView(targetUrl);
    }

    /**
     * Convert AffiliateLink to DTO
     */
    private AffiliateLinkDto convertToDto(AffiliateLink link) {
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

        // Product info (nullable)
        if (link.getProduct() != null) {
            dto.setProductId(link.getProduct().getId());
            dto.setProductTitle(link.getProduct().getTitle());
            dto.setProductPrice(Double.valueOf(link.getProduct().getSellingPrice()));
            if (link.getProduct().getImages() != null && !link.getProduct().getImages().isEmpty()) {
                dto.setProductImage(link.getProduct().getImages().get(0));
            }
        }

        // KOC info
        dto.setKocCode(link.getKoc().getKocCode());
        dto.setKocName(link.getKoc().getCustomer().getFullName());

        return dto;
    }
}
