package com.zosh.service.impl;

import com.zosh.model.VerificationCode;
import com.zosh.repository.VerificationCodeRepository;
import com.zosh.service.VerificationService;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Date;

@Service
public class VerificationServiceImpl implements VerificationService {

    private final VerificationCodeRepository verificationCodeRepository;

    VerificationServiceImpl(VerificationCodeRepository verificationCodeRepository){

        this.verificationCodeRepository = verificationCodeRepository;
    }

    @Override
    public VerificationCode createVerificationCode(String otp,String username, String email) {
        VerificationCode isExist=verificationCodeRepository.findByUsername(username);

        // // Xoá bản cũ nếu có
        if(isExist!=null) {
            verificationCodeRepository.delete(isExist);
        }

        VerificationCode verificationCode=new VerificationCode();
        verificationCode.setOtp(otp);
        verificationCode.setUsername(username);
        verificationCode.setEmail(email); // <-- Lưu lại để gửi mail

        // Gán thời gian tạo và thời gian hết hạn
        Date now = new Date();
        verificationCode.setCreatedAt(now);

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(now);
        calendar.add(Calendar.MINUTE, 10); // OTP hết hạn sau 10 phút
        verificationCode.setExpiresAt(calendar.getTime());
        return verificationCodeRepository.save(verificationCode);

    }

    @Override
    public VerificationCode findByUsername(String username) {
        return verificationCodeRepository.findByUsername(username);
    }
}
