package com.zosh.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SignUpRequest {
    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không hợp lệ")
    private String email;

    @NotBlank(message = "Username không được để trống")
    @Size(max = 50, message = "Username không được vượt quá 50 ký tự")
    @Size(min = 3, message = "Username không được ít hơn 3 ký tự")
    private String username;

    @NotBlank(message = "Password không được để trống")
    @Size(min = 6, message = "Mật khẩu phải ít nhất 6 ký tự")
    private String password;

    private String otp;
}
