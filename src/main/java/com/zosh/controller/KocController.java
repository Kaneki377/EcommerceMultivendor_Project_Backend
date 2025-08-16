package com.zosh.controller;

import com.zosh.config.JwtProvider;
import com.zosh.dto.AffiliateRegistrationResponse;
import com.zosh.dto.CommissionDTO;
import com.zosh.exceptions.KocException;
import com.zosh.exceptions.SellerException;
import com.zosh.model.AffiliateRegistration;
import com.zosh.model.Koc;
import com.zosh.repository.KocRepository;
import com.zosh.repository.OrderItemRepository;
import com.zosh.repository.PayoutItemRepository;
import com.zosh.request.CreateAffiliateLinkRequest;
import com.zosh.request.CreateKocRequest;
import com.zosh.response.AffiliateLinkResponse;
import com.zosh.service.AffiliateLinkService;
import com.zosh.service.AffiliateRegistrationService;
import com.zosh.service.KocService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/koc")
@RequiredArgsConstructor
public class KocController {

    private final KocService kocService;
    private final AffiliateLinkService affiliateLinkService;
    private final JwtProvider jwtProvider;
    private final KocRepository kocRepository;
    private final AffiliateRegistrationService registrationService;
    private final OrderItemRepository orderItemRepository;
    private final PayoutItemRepository payoutItemRepository;

    @PostMapping("/create")
    public ResponseEntity<?> createKoc(@Valid @RequestBody CreateKocRequest request) {
        if (!request.hasAtLeastOneLink()) {
            return ResponseEntity.badRequest().body("Phải cung cấp ít nhất một link mạng xã hội.");
        }

        Koc koc = kocService.createKoc(request);
        return ResponseEntity.ok(koc);
    }


    @GetMapping("/test")
    @PreAuthorize("hasRole('KOC')")
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
            @RequestBody @Valid CreateAffiliateLinkRequest req,
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

    @GetMapping("/commissions/payable")
    @PreAuthorize("hasRole('KOC')")
    public ResponseEntity<List<CommissionDTO>> myPayable(@RequestHeader("Authorization") String jwt) {
        String username = jwtProvider.getUsernameFromJwtToken(jwt);
        Long kocId = kocRepository.findByCustomer_Account_Username(username)
                .orElseThrow(() -> new KocException("KOC doesn't exist"))
                .getId();

        var items = orderItemRepository.findPayableByKoc(kocId).stream()
                .filter(oi -> !payoutItemRepository.existsByOrderItem_Id(oi.getId()))
                .map(oi -> CommissionDTO.builder()
                        .orderItemId(oi.getId())
                        .orderId(oi.getOrder().getId())
                        .productId(oi.getProduct().getId())
                        .campaignId(oi.getAffiliateLink().getCampaign().getId())
                        .kocId(oi.getAffiliateLink().getKoc().getId())
                        .commissionAmount(oi.getCommissionAmount())
                        .status(oi.getCommissionStatus())
                        .attributedAt(oi.getAttributedAt())
                        .build())
                .toList();

        return ResponseEntity.ok(items);
    }

}