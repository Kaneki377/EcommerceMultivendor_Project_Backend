package com.zosh.controller;

import com.zosh.model.AffiliateLink;
import com.zosh.repository.AffiliateLinkRepository;
import com.zosh.service.AffiliateCampaignService;
import com.zosh.service.AffiliateLinkService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class RedirectController {

    private final AffiliateLinkService affiliateLinkService;
    private final AffiliateLinkRepository affiliateLinkRepository;
    private final AffiliateCampaignService affiliateCampaignService;

    // Ví dụ: /r/l/123   (123 = id của AffiliateLink)
    // /r/{shortToken}
    @GetMapping("/r/{token:[0-9A-Za-z]{8,22}}") //Tranh path injection
    @Transactional
    public ResponseEntity<Void> redirect(@PathVariable("token") String token) {
        var link = affiliateLinkService.getByShortToken(token);

        // chặn nếu campaign/link không active
        if (!affiliateCampaignService.isActive(link.getCampaign())) {
            return ResponseEntity.status(HttpStatus.GONE).build(); // 410 Gone
        }

        // Tăng đếm click (atomic)
        affiliateLinkRepository.incrementClick(link.getId());

        HttpHeaders headers = new HttpHeaders();
        headers.add("Location", link.getTargetUrl()); // ví dụ /products/123?koc=KOC001&cmp=AFF-SELLER-1
        return new ResponseEntity<>(headers, HttpStatus.FOUND);
    }
}
