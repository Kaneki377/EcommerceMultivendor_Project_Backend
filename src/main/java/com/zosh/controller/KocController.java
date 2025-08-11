package com.zosh.controller;

import com.zosh.config.JwtProvider;
import com.zosh.dto.AffiliateRegistrationResponse;
import com.zosh.exceptions.KocException;
import com.zosh.exceptions.SellerException;
import com.zosh.model.AffiliateRegistration;
import com.zosh.model.Koc;
import com.zosh.repository.KocRepository;
import com.zosh.request.ClickQueryRequest;
import com.zosh.request.CreateAffiliateLinkRequest;
import com.zosh.request.CreateKocRequest;
import com.zosh.response.AffiliateLinkResponse;
import com.zosh.response.ClickEventResponse;
import com.zosh.response.ClickStatsResponse;
import com.zosh.service.AffiliateLinkService;
import com.zosh.service.AffiliateRegistrationService;
import com.zosh.service.ClickEventService;
import com.zosh.service.KocService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/koc")
@RequiredArgsConstructor
public class KocController {

    private final KocService kocService;
    private final AffiliateLinkService affiliateLinkService;
    private final ClickEventService clickEventService;
    private final JwtProvider jwtProvider;
    private final KocRepository kocRepository;
    private final AffiliateRegistrationService registrationService;

    @PostMapping("/create")
    public ResponseEntity<?> createKoc(@Valid @RequestBody CreateKocRequest request) {
        if (!request.hasAtLeastOneLink()) {
            return ResponseEntity.badRequest().body("Phải cung cấp ít nhất một link mạng xã hội.");
        }

        Koc koc = kocService.createKoc(request);
        return ResponseEntity.ok(koc);
    }


    @GetMapping("/test")
    @PreAuthorize("hasRole('ROLE_KOC')")
    public ResponseEntity<String> testKocRole() {
        return ResponseEntity.ok("Access granted for ROLE_KOC");
    }
    // KOC đăng ký chiến dịch
    @PostMapping("/register-campaign/{campaignId}")
    @PreAuthorize("hasRole('KOC')")
    public ResponseEntity<AffiliateRegistration> registerToCampaign(
            @PathVariable Long campaignId,
            @RequestHeader("Authorization") String jwt) throws SellerException {

        AffiliateRegistration registration = registrationService.registerCampaign(campaignId, jwt);
        return ResponseEntity.ok(registration);
    }

    // KOC xem các đăng ký của chính mình
    @GetMapping("/my-registrations")
    @PreAuthorize("hasRole('KOC')")
    public ResponseEntity<List<AffiliateRegistrationResponse>> getMyRegistrations(
            @RequestHeader("Authorization") String jwt) {

        List<AffiliateRegistrationResponse> list = registrationService.getMyRegistrations(jwt);
        return ResponseEntity.ok(list);
    }

    private Long currentKocId(String jwt) {
        String username = jwtProvider.getUsernameFromJwtToken(jwt);
        Koc koc = kocRepository.findByCustomer_Account_Username(username)
                .orElseThrow(() -> new KocException("KOC doesn't exist"));
        return koc.getId();
    }

    @PostMapping("/affiliate-links")
    @PreAuthorize("hasRole('KOC')")
    public ResponseEntity<AffiliateLinkResponse> createLink(
            @RequestBody CreateAffiliateLinkRequest req,
            @RequestHeader("Authorization") String jwt) {

        Long kocId = currentKocId(jwt);
        var res = affiliateLinkService.createAffiliateLink(
                kocId,
                req.getCampaignId(),
                req.getProductId(),
                req.getTargetUrl()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(res);
    }

    @GetMapping("/affiliate-links")
    @PreAuthorize("hasRole('KOC')")
    public ResponseEntity<List<AffiliateLinkResponse>> myLinks(
            @RequestHeader("Authorization") String jwt) {
        Long kocId = currentKocId(jwt);
        return ResponseEntity.ok(affiliateLinkService.getLinksByKoc(kocId));
    }

    @GetMapping("/affiliate-links/{linkId}/stats")
    @PreAuthorize("hasRole('KOC')")
    public ResponseEntity<ClickStatsResponse> linkStats(
            @PathVariable Long linkId,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestHeader("Authorization") String jwt) {

        // chỉ convert nhanh – bạn có thể dùng formatter nếu cần
        LocalDateTime fromDt = (from != null) ? LocalDateTime.parse(from) : null;
        LocalDateTime toDt = (to != null) ? LocalDateTime.parse(to) : null;

        return ResponseEntity.ok(affiliateLinkService.getLinkStats(linkId, fromDt, toDt));
    }

    @GetMapping("/affiliate-links/{linkId}/clicks")
    @PreAuthorize("hasRole('KOC')")
    public ResponseEntity<List<ClickEventResponse>> listClicks(
            @PathVariable Long linkId,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestHeader("Authorization") String jwt) {

        ClickQueryRequest req = new ClickQueryRequest();
        req.setPage(page);
        req.setSize(size);
        if (from != null) req.setFrom(LocalDateTime.parse(from));
        if (to != null) req.setTo(LocalDateTime.parse(to));

        return ResponseEntity.ok(clickEventService.listClicksOfLink(linkId, req));
    }
}