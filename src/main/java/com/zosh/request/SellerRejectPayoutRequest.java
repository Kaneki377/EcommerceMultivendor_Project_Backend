package com.zosh.request;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Data
@Getter
@Setter
public class SellerRejectPayoutRequest {

    private List<Long> commissionIds;
    private String note;
}
