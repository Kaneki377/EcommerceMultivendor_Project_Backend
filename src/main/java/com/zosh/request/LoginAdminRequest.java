package com.zosh.request;

import lombok.Data;

@Data
public class LoginAdminRequest {
    private String username;
    private String password;
    private String email;
    private String otp;
}
