package com.zosh.request;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Data
@Getter
@Setter
public class SellerApprovePayoutRequest {

    private List<Long> commissionIds; // bắt buộc
    private String transactionId;     // mã giao dịch ngân hàng
    private String note;
}
