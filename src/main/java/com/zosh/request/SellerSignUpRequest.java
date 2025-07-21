package com.zosh.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SellerSignUpRequest {

    @Valid
    private SignUpRequest account;

    @NotBlank(message = "sellerName")
    private String sellerName;

    @Valid
    @NotNull(message = "Thông tin doanh nghiệp không được để trống")
    private BusinessDetailRequest businessDetails;

    @Valid
    @NotNull(message = "Thông tin ngân hàng không được để trống")
    private BankDetailRequest bankDetails;

    @Valid
    @NotNull(message = "Địa chỉ lấy hàng không được để trống")
    private AddressRequest pickupAddress;

    @NotBlank(message = "Mã số thuế lấy hàng không được để trống")
    private String taxCode;

    @NotBlank(message = "Số điện thoại lấy hàng không được để trống")
    private String mobile;
}

