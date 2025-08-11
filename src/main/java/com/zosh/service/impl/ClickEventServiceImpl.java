package com.zosh.service.impl;

import com.zosh.model.AffiliateLink;
import com.zosh.model.ClickEvent;
import com.zosh.repository.ClickEventRepository;
import com.zosh.request.ClickQueryRequest;
import com.zosh.response.ClickEventResponse;
import com.zosh.service.ClickEventService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClickEventServiceImpl implements ClickEventService {

    private final ClickEventRepository clickEventRepository;

    @Override
    public void recordClick(AffiliateLink link, HttpServletRequest request) {

        String ip = realIp(request);
        String ua = headerOrNull(request, "User-Agent");
        String ref = headerOrNull(request, "Referer");
        String session = request.getSession(false) != null ? request.getSession(false).getId() : null;

        ClickEvent e = new ClickEvent();
        e.setAffiliateLink(link);
        e.setKoc(link.getKoc());
        e.setCampaign(link.getCampaign());
        e.setProduct(link.getProduct());
        e.setIp(ip);
        e.setUserAgent(ua);
        e.setReferrer(ref);
        e.setSessionId(session);
        e.setCreatedAt(LocalDateTime.now());
        clickEventRepository.save(e);
    }

    @Override
    public List<ClickEventResponse> listClicksOfLink(Long linkId, ClickQueryRequest request) {
        LocalDateTime from = request.getFrom();
        LocalDateTime to = request.getTo();
        var pageable = PageRequest.of(Math.max(request.getPage(), 0), Math.max(request.getSize(), 1));

        var page = (from != null && to != null)
                ? clickEventRepository.findByAffiliateLink_IdAndCreatedAtBetweenOrderByCreatedAtDesc(linkId, from, to, pageable)
                : clickEventRepository.findByAffiliateLink_IdOrderByCreatedAtDesc(linkId, pageable);

        return page.getContent().stream().map(e ->
                ClickEventResponse.builder()
                        .id(e.getId())
                        .linkId(e.getAffiliateLink().getId())
                        .campaignId(e.getCampaign().getId())
                        .productId(e.getProduct() != null ? e.getProduct().getId() : null)
                        .maskedIp(maskIp(e.getIp()))
                        .userAgent(e.getUserAgent())
                        .referrer(e.getReferrer())
                        .sessionId(e.getSessionId())
                        .createdAt(e.getCreatedAt())
                        .build()
        ).collect(Collectors.toList());
    }

    // ===== helpers =====
    private String headerOrNull(HttpServletRequest req, String name) {
        String v = req.getHeader(name);
        return (v == null || v.isBlank()) ? null : v;
    }

    private String realIp(HttpServletRequest request) {
        String h = request.getHeader("X-Forwarded-For");
        if (h != null && !h.isBlank()) {
            // lấy IP đầu tiên
            return h.split(",")[0].trim();
        }
        h = request.getHeader("X-Real-IP");
        if (h != null && !h.isBlank()) return h.trim();
        return request.getRemoteAddr();
    }

    private String maskIp(String ip) {
        if (ip == null) return null;
        // đơn giản: 1.2.3.4 -> 1.2.3.*
        int lastDot = ip.lastIndexOf('.');
        if (lastDot > 0) return ip.substring(0, lastDot) + ".*";
        return ip;
    }
}
