package com.zosh.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SignUpRequest {
    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không hợp lệ")
    private String email;

    @NotBlank(message = "Họ tên không được để trống")
    private String fullName;

    @NotBlank(message = "OTP không được để trống")
    private String otp;
}
