package com.zosh.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class BusinessDetailRequest {
    @NotBlank(message = "Tên doanh nghiệp không được để trống")
    private String businessName;


    private String businessEmail;

    private String businessLicenseUrl;

    private String businessMobile;

    private String businessAddress;

    private String logo;

    private String banner;
}
