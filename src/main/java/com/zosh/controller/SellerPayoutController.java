package com.zosh.controller;

import com.zosh.dto.CommissionDTO;
import com.zosh.request.SellerApprovePayoutRequest;
import com.zosh.request.SellerRejectPayoutRequest;
import com.zosh.response.PayoutSummaryResponse;
import com.zosh.service.CommissionPayoutService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/seller/payouts")
public class SellerPayoutController {

    private final CommissionPayoutService commissionPayoutService;

    @GetMapping("/requests")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<List<CommissionDTO>> listRequests(@RequestHeader("Authorization") String jwt) {
        return ResponseEntity.ok(commissionPayoutService.sellerListPayoutRequests(jwt));
    }

    @PostMapping("/approve")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<?> approve(
            @RequestHeader("Authorization") String jwt,
            @RequestBody SellerApprovePayoutRequest req
    ) {
        if (req.getCommissionIds() == null || req.getCommissionIds().isEmpty()) {
            return ResponseEntity.badRequest().body("commissionIds is required");
        }
        List<CommissionDTO> result = commissionPayoutService
                .sellerApprovePayout(jwt, req.getCommissionIds(), req.getTransactionId(), req.getNote());
        return ResponseEntity.ok(result);
    }

    @PostMapping("/reject")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<?> reject(
            @RequestHeader("Authorization") String jwt,
            @RequestBody SellerRejectPayoutRequest req
    ) {
        if (req.getCommissionIds() == null || req.getCommissionIds().isEmpty()) {
            return ResponseEntity.badRequest().body("commissionIds is required");
        }
        List<CommissionDTO> result = commissionPayoutService
                .sellerRejectPayout(jwt, req.getCommissionIds(), req.getNote());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/summary")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<PayoutSummaryResponse> summary(@RequestHeader("Authorization") String jwt) {
        return ResponseEntity.ok(commissionPayoutService.sellerPayoutSummary(jwt));
    }
}
