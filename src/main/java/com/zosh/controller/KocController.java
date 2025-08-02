package com.zosh.controller;

import com.zosh.dto.AffiliateRegistrationResponse;
import com.zosh.exceptions.SellerException;
import com.zosh.model.AffiliateRegistration;
import com.zosh.model.Koc;
import com.zosh.request.CreateKocRequest;
import com.zosh.service.AffiliateRegistrationService;
import com.zosh.service.KocService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/koc")
@RequiredArgsConstructor
public class KocController {

    private final KocService kocService;
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
}