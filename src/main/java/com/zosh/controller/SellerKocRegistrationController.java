package com.zosh.controller;

import com.zosh.dto.KocRegistrationDto;
import com.zosh.dto.RegistrationApprovalResponse;
import com.zosh.exceptions.SellerException;
import com.zosh.service.AffiliateRegistrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sellers")
@RequiredArgsConstructor
public class SellerKocRegistrationController {

    private final AffiliateRegistrationService registrationService;

    // SELLER xem các Koc đăng ký chiến dịch của mình
    @GetMapping("/campaign-registrations")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<List<KocRegistrationDto>> getRegistrationsForMyCampaigns(
            @RequestHeader("Authorization") String jwt) throws SellerException {

        List<KocRegistrationDto> list = registrationService.getRegistrationsForMyCampaigns(jwt);
        return ResponseEntity.ok(list);
    }

    // SELLER approve đăng ký
    @PostMapping("/affiliate-registrations/approve/{id}")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<RegistrationApprovalResponse> approveRegistration(
            @PathVariable Long id,
            @RequestHeader("Authorization") String jwt) throws Exception {

        RegistrationApprovalResponse response = registrationService.approveRegistration(id, jwt);
        return ResponseEntity.ok(response);
    }

    // SELLER reject đăng ký
    @PostMapping("/affiliate-registrations/reject/{id}")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<RegistrationApprovalResponse> rejectRegistration(
            @PathVariable Long id,
            @RequestHeader("Authorization") String jwt) throws Exception {

        RegistrationApprovalResponse response = registrationService.rejectRegistration(id, jwt);
        return ResponseEntity.ok(response);
    }
}
