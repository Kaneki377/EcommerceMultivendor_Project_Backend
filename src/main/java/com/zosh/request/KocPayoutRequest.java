package com.zosh.request;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Data
@Getter
@Setter
public class KocPayoutRequest {

    // Nếu null/empty và allEligible=true => pick tất cả commission đủ điều kiện
    private List<Long> commissionIds;
    private boolean allEligible = false;
}
