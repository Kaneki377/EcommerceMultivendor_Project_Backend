package com.zosh.request;

import lombok.Data;

@Data
public class SignUpRequest {
    private String email;
    private String fullName;
    private String otp;
}
