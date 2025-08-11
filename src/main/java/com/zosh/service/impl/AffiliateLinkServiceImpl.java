package com.zosh.service.impl;

import com.zosh.domain.RegistrationStatus;
import com.zosh.exceptions.KocException;
import com.zosh.model.AffiliateCampaign;
import com.zosh.model.AffiliateLink;
import com.zosh.model.Koc;
import com.zosh.model.Product;
import com.zosh.repository.*;
import com.zosh.response.AffiliateLinkResponse;
import com.zosh.response.ClickStatsResponse;
import com.zosh.service.AffiliateLinkService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AffiliateLinkServiceImpl implements AffiliateLinkService {

    private final AffiliateLinkRepository affiliateLinkRepository;
    private final KocRepository kocRepository;
    private final AffiliateCampaignRepository campaignRepository;
    private final ProductRepository productRepository;
    private final ClickEventRepository clickEventRepository;
    private final AffiliateRegistrationRepository registrationRepository;

    @Override
    public AffiliateLinkResponse createAffiliateLink(Long kocId, Long campaignId, Long productId, String targetUrl) {
        Koc koc = kocRepository.findById(kocId)
                .orElseThrow(() -> new KocException("KOC not found"));

        AffiliateCampaign campaign = campaignRepository.findById(campaignId)
                .orElseThrow(() -> new RuntimeException("Campaign not found"));

        // KOC phải đã được APPROVED trong campaign
        boolean approved = registrationRepository
                .existsByKoc_IdAndCampaign_IdAndStatus(kocId, campaignId, RegistrationStatus.APPROVED);

        if (!approved) {
            throw new RuntimeException("KOC is not approved for this campaign");
        }

        Product product = null;
        if (productId != null) {
            product = productRepository.findById(productId)
                    .orElseThrow(() -> new RuntimeException("Product not found"));

            // Kiểm tra product thuộc đúng campaign (Many-to-One)
            if (product.getAffiliateCampaign() == null ||
                    !product.getAffiliateCampaign().getId().equals(campaignId)) {
                throw new RuntimeException("Product is not in this campaign");
            }
        }

        // Sinh code ngắn duy nhất
        String code = generateShortCode();

        String resolvedTarget = resolveTargetUrl(targetUrl, campaign, product);
        String generatedUrl = "/r/" + code;

        AffiliateLink link = new AffiliateLink();
        link.setKoc(koc);
        link.setCampaign(campaign);
        link.setProduct(product);
        link.setCode(code);
        link.setTargetUrl(resolvedTarget);
        link.setGeneratedUrl(generatedUrl);
        link.setCreatedAt(LocalDateTime.now());

        affiliateLinkRepository.save(link);

        return AffiliateLinkResponse.builder()
                .id(link.getId())
                .campaignId(campaignId)
                .productId(productId)
                .code(code)
                .targetUrl(resolvedTarget)
                .generatedUrl(generatedUrl)
                .createdAt(link.getCreatedAt())
                .totalClicks(0L)
                .build();
    }

    @Override
    public List<AffiliateLinkResponse> getLinksByKoc(Long kocId) {
        return affiliateLinkRepository.findByKoc_Id(kocId)
                .stream()
                .map(l -> AffiliateLinkResponse.builder()
                        .id(l.getId())
                        .campaignId(l.getCampaign().getId())
                        .productId(l.getProduct() != null ? l.getProduct().getId() : null)
                        .code(l.getCode())
                        .targetUrl(l.getTargetUrl())
                        .generatedUrl(l.getGeneratedUrl())
                        .createdAt(l.getCreatedAt())
                        .totalClicks(clickEventRepository.countByAffiliateLink_Id(l.getId()))
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public AffiliateLink getByCode(String code) {
        return affiliateLinkRepository.findByCode(code)
                .orElseThrow(() -> new RuntimeException("Affiliate link not found"));
    }

    @Override
    public ClickStatsResponse getLinkStats(Long linkId, LocalDateTime from, LocalDateTime to) {
        // khoảng mặc định 30 ngày gần nhất
        if (from == null || to == null) {
            to = LocalDateTime.now();
            from = to.minusDays(30);
        }

        long total = clickEventRepository.countByAffiliateLink_IdAndCreatedAtBetween(linkId, from, to);
        long uniqueSessions = clickEventRepository.countDistinctSessionId(linkId, from, to);

        Map<LocalDate, Long> byDate = new LinkedHashMap<>();
        List<Object[]> rows = clickEventRepository.aggregateDaily(linkId, from, to);
        for (Object[] row : rows) {
            // row[0] là java.sql.Date hoặc LocalDate (tuỳ JPA provider)
            LocalDate d = (row[0] instanceof LocalDate)
                    ? (LocalDate) row[0]
                    : ((java.sql.Date) row[0]).toLocalDate();
            long cnt = ((Number) row[1]).longValue();
            byDate.put(d, cnt);
        }

        return ClickStatsResponse.builder()
                .linkId(linkId)
                .totalClicks(total)
                .uniqueSessions(uniqueSessions)
                .clicksByDate(byDate)
                .build();
    }

    // ========== helpers ==========
    private String generateShortCode() {
        // 8 ký tự base36
        String code = Long.toString(Math.abs(new Random().nextLong()), 36);
        code = code.substring(0, Math.min(8, code.length())).toUpperCase();
        // đảm bảo duy nhất
        while (affiliateLinkRepository.findByCode(code).isPresent()) {
            code = Long.toString(Math.abs(new Random().nextLong()), 36);
            code = code.substring(0, Math.min(8, code.length())).toUpperCase();
        }
        return code;
    }

    private String resolveTargetUrl(String targetUrl, AffiliateCampaign campaign, Product product) {
        if (StringUtils.hasText(targetUrl)) return targetUrl;
        if (product != null) return "/products/" + product.getId();
        return "/campaigns/" + campaign.getId();
    }
}
