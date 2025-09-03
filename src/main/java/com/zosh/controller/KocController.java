package com.zosh.controller;

import com.zosh.dto.AffiliateCampaignDto;
import com.zosh.dto.AffiliateRegistrationResponse;
import com.zosh.dto.KocCommissionDto;
import com.zosh.exceptions.SellerException;
import com.zosh.model.AffiliateCommission;
import com.zosh.model.AffiliateRegistration;
import com.zosh.model.Customer;
import com.zosh.model.Koc;
import com.zosh.request.CreateKocRequest;
import com.zosh.service.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/koc")
@RequiredArgsConstructor
public class KocController {

    private final KocService kocService;
    private final AffiliateRegistrationService registrationService;
    private final AffiliateCampaignService affiliateCampaignService;
    private final AffiliateCommissionService commissionService;
    private final CustomerService customerService; // <-- THÊM
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
    public ResponseEntity<AffiliateRegistrationResponse> registerToCampaign(
            @PathVariable Long campaignId,
            @RequestHeader("Authorization") String jwt) throws SellerException {

        AffiliateRegistration registration = registrationService.registerCampaign(campaignId, jwt);

        // Convert to DTO to avoid Hibernate proxy serialization issues
        AffiliateRegistrationResponse response = new AffiliateRegistrationResponse(
                registration.getId(),
                registration.getCampaign().getId(),
                registration.getCampaign().getName(),
                registration.getRegisteredAt(),
                registration.getStatus());

        return ResponseEntity.ok(response);
    }

    // KOC xem các đăng ký của chính mình
    @GetMapping("/my-registrations")
    @PreAuthorize("hasRole('KOC')")
    public ResponseEntity<List<AffiliateRegistrationResponse>> getMyRegistrations(
            @RequestHeader("Authorization") String jwt) {

        List<AffiliateRegistrationResponse> list = registrationService.getMyRegistrations(jwt);
        return ResponseEntity.ok(list);
    }

    // Xem các danh sách affiliate active
    @GetMapping("/affiliate-campaign/active")
    public ResponseEntity<List<AffiliateCampaignDto>> getActiveCampaigns() {
        List<AffiliateCampaignDto> result = affiliateCampaignService.getActiveCampaigns()
                .stream()
                .map(c -> new AffiliateCampaignDto(
                        c.getId(),
                        c.getCampaignCode(),
                        c.getName(),
                        c.getDescription(),
                        c.getCommissionPercent(),
                        c.getCreatedAt(),
                        c.getExpiredAt(),
                        c.getActive()))
                .toList();

        return ResponseEntity.ok(result);
    }

    // KOC xem dashboard commission
    @GetMapping("/commission/dashboard")
    @PreAuthorize("hasRole('KOC')")
    public ResponseEntity<Map<String, Object>> getCommissionDashboard(
            @RequestHeader("Authorization") String jwt) {

        var customer = customerService.findCustomerProfileByJwt(jwt);
        var koc = kocService.getByCustomerId(customer.getId()); // viết 1 hàm service đơn giản
        Map<String, Object> dashboardData = commissionService.getKocDashboardData(koc.getId());
        return ResponseEntity.ok(dashboardData);
    }

    // KOC xem lịch sử commission
    @GetMapping("/commission/history")
    @PreAuthorize("hasRole('KOC')")
    public ResponseEntity<List<KocCommissionDto>> getCommissionHistory(
            @RequestHeader("Authorization") String jwt) {

        Customer customer = customerService.findCustomerProfileByJwt(jwt);
        Koc koc = kocService.getByCustomerId(customer.getId());

        List<AffiliateCommission> commissions = commissionService.getKocCommissions(koc.getId());
        List<KocCommissionDto> result = commissions.stream()
                .map(c -> new KocCommissionDto(
                        c.getId(),
                        c.getOrderItem().getOrder().getOrderId(),
                        c.getOrderValue(),
                        c.getCommissionPercent(),
                        c.getCommissionAmount(),
                        c.getStatus(),
                        c.getCreatedAt(),
                        c.getPaidAt(),
                        c.getCampaign().getName(),
                        c.getCampaign().getCampaignCode(),
                        c.getAffiliateLink().getProduct() != null ? c.getAffiliateLink().getProduct().getTitle() : null,
                        c.getAffiliateLink().getProduct() != null ? c.getAffiliateLink().getProduct().getImages().get(0) : null
                ))
                .toList();

        return ResponseEntity.ok(result);
    }

}