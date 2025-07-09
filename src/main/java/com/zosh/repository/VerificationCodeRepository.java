package com.zosh.repository;

import com.zosh.model.VerificationCode;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VerificationCodeRepository extends JpaRepository<VerificationCode, Long> {

        VerificationCode findByEmail(String email);// SELECT * FROM verification_code WHERE email = ? LIMIT 1

        VerificationCode findByOtp(String otp);
}
