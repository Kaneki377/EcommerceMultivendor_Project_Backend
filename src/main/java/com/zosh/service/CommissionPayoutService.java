package com.zosh.service;

import com.zosh.dto.CommissionDTO;
import com.zosh.response.PayoutSummaryResponse;

import java.util.List;

public interface CommissionPayoutService {

    // SELLER
    List<CommissionDTO> sellerListPayoutRequests(String jwt);
    List<CommissionDTO> sellerApprovePayout(String jwt, List<Long> orderItemIds, String transactionId, String note);
    List<CommissionDTO> sellerRejectPayout(String jwt, List<Long> orderItemIds, String note);
    PayoutSummaryResponse sellerPayoutSummary(String jwt);

    // KOC (tạo yêu cầu – nếu muốn, thêm endpoint ở KocController)
    void kocRequestPayout(String jwt, List<Long> orderItemIds, boolean allEligible);
}
