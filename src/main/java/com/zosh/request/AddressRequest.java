package com.zosh.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AddressRequest {

    private String name;

    @NotBlank(message = "Khu vực không được để trống")
    private String locality;

    @NotBlank(message = "Đường không được để trống")
    private String street;

    @NotBlank(message = "Thành phố không được để trống")
    private String city;

    @NotBlank(message = "Tỉnh/Quận không được để trống")
    private String state;

    private String postalCode;

    private String mobile;
}
