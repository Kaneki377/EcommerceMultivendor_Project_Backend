package com.zosh.verification;

import com.zosh.model.VerificationCode;
import com.zosh.service.VerificationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@Transactional
public class VerificationServiceImpl {

    @Autowired
    private VerificationService verificationService;

    @Test
    void testCreateVerificationCode() {
        VerificationCode code = verificationService.createVerificationCode("123456", "testuser", "user@email.com");

        assertNotNull(code);
        assertEquals("testuser", code.getUsername());
        assertEquals("123456", code.getOtp());

        VerificationCode found = verificationService.findByUsername("testuser");
        assertNotNull(found);
    }
}
