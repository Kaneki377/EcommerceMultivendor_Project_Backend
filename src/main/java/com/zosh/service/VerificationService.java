package com.zosh.service;

import com.zosh.model.VerificationCode;

public interface VerificationService {

    VerificationCode createVerificationCode(String otp, String username, String email);
    VerificationCode findByUsername(String username);

}
