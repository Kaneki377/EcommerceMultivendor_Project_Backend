package com.zosh.response;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Data
@Getter
@Setter
@Builder
public class PayoutSummaryResponse {

    private BigDecimal totalRequested; // tổng hoa hồng đã yêu cầu payout
    private BigDecimal totalPaid;      // tổng hoa hồng đã trả
    private BigDecimal totalPending;
}
