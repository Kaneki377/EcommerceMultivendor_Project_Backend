package com.zosh.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class BusinessDetailRequest {
    @NotBlank(message = "Tên doanh nghiệp không được để trống")
    private String businessName;

    @NotBlank(message = "Email doanh nghiệp không được để trống")
    @Email(message = "Email doanh nghiệp không hợp lệ")
    private String businessEmail;

    @NotBlank(message = "Số điện thoại doanh nghiệp không được để trống")
    private String businessMobile;

    @NotBlank(message = "Địa chỉ doanh nghiệp không được để trống")
    private String businessAddress;

    private String logo;

    private String banner;
}
