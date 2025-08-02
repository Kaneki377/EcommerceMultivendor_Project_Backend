package com.zosh.controller;

import com.zosh.dto.AffiliateRegistrationResponse;
import com.zosh.dto.RegistrationApprovalResponse;
import com.zosh.exceptions.SellerException;
import com.zosh.model.AffiliateCampaign;
import com.zosh.model.AffiliateRegistration;
import com.zosh.model.Seller;
import com.zosh.request.CreateAffiliateCampaignRequest;
import com.zosh.service.AffiliateCampaignService;
import com.zosh.service.AffiliateRegistrationService;
import com.zosh.service.SellerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/sellers")
@RequiredArgsConstructor
public class SellerCampaignController {

    private final SellerService sellerService;
    private final AffiliateRegistrationService registrationService;
    private final AffiliateCampaignService affiliateCampaignService;

    //Seller tạo chiến dịch
    @PostMapping("/campaigns")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<AffiliateCampaign> createAffiliateCampaign(
            @Valid @RequestBody CreateAffiliateCampaignRequest request,
            @RequestHeader("Authorization") String jwt) throws Exception {

        Seller seller = sellerService.getSellerProfile(jwt);

        AffiliateCampaign campaign = affiliateCampaignService.createCampaign(seller.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(campaign);
    }
    //Seller xem các chiến dịch đã tạo
    @GetMapping("/campaigns")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<List<AffiliateCampaign>> getMyCampaigns(
            @RequestHeader("Authorization") String jwt) throws SellerException {
        return ResponseEntity.ok(affiliateCampaignService.getCampaignsBySeller(jwt));
    }
    //Seller cập nhật chiến dịch
    @PatchMapping("/campaigns/{id}")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<AffiliateCampaign> partialUpdateCampaign(
            @PathVariable Long id,
            @RequestBody Map<String, Object> updates,
            @RequestHeader("Authorization") String jwt) throws Exception {

        Seller seller = sellerService.getSellerProfile(jwt);
        AffiliateCampaign updated = affiliateCampaignService.partialUpdate(id, seller.getId(), updates);
        return ResponseEntity.ok(updated);
    }
    //Seller xóa chiến dịch
    @DeleteMapping("/campaigns/{id}")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<?> deleteCampaign(
            @PathVariable Long id,
            @RequestHeader("Authorization") String jwt) throws Exception {

        Seller seller = sellerService.getSellerProfile(jwt);
        affiliateCampaignService.deleteCampaign(id, seller.getId());

        return ResponseEntity.ok().body("Campaign deleted successfully");
    }

    // SELLER xem các Koc đăng ký  chiến dịch của mình
    @GetMapping("/campaign-registrations")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<List<AffiliateRegistration>> getRegistrationsForMyCampaigns(
            @RequestHeader("Authorization") String jwt) throws SellerException {

        List<AffiliateRegistration> list = registrationService.getRegistrationsForMyCampaigns(jwt);
        return ResponseEntity.ok(list);
    }

    // SELLER duyệt KOC
    @PutMapping("/affiliate-registrations/approve/{registrationId}")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<RegistrationApprovalResponse> approveRegistration(
            @PathVariable Long registrationId,
            @RequestHeader("Authorization") String jwt) throws SellerException {

        RegistrationApprovalResponse registration = registrationService.approveRegistration(registrationId, jwt);
        return ResponseEntity.ok(registration);
    }
    // SELLER từ chối KOC
    @PutMapping("/affiliate-registrations/reject/{registrationId}")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<RegistrationApprovalResponse> rejectRegistration(
            @PathVariable Long registrationId,
            @RequestHeader("Authorization") String jwt) throws SellerException {

        RegistrationApprovalResponse registration = registrationService.rejectRegistration(registrationId, jwt);
        return ResponseEntity.ok(registration);
    }
}
