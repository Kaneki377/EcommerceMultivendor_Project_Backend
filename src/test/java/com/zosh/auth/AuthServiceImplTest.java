package com.zosh.auth;

import com.zosh.domain.USER_ROLE;
import com.zosh.model.Account;
import com.zosh.model.Role;
import com.zosh.model.VerificationCode;
import com.zosh.repository.AccountRepository;
import com.zosh.repository.RoleRepository;
import com.zosh.repository.VerificationCodeRepository;
import com.zosh.request.CustomerSignUpRequest;
import com.zosh.request.SignUpRequest;
import com.zosh.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
public class AuthServiceImplTest {

    @Autowired
    private AuthService authService;
    @Autowired private AccountRepository accountRepository;
    @Autowired private RoleRepository roleRepository;
    @Autowired private VerificationCodeRepository verificationCodeRepository;

    @Autowired private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setup() {
        // Khởi tạo Role trước
        Role role = new Role();
        role.setName(USER_ROLE.ROLE_CUSTOMER.name());
        roleRepository.save(role);
    }

    @Test
    void testCreateUser_withValidOtp_shouldCreateAccountAndReturnToken() throws Exception {
        // 1. Setup: giả lập mã OTP đã tồn tại trong DB
        String email = "newuser@example.com";
        String username = "newuser";
        String otp = "123456";

        VerificationCode verificationCode = new VerificationCode();
        verificationCode.setEmail(email);
        verificationCode.setOtp(otp);
        verificationCodeRepository.save(verificationCode);

        // 2. Setup request đăng ký user
        SignUpRequest accReq = new SignUpRequest();
        accReq.setEmail(email);
        accReq.setUsername(username);
        accReq.setPassword("password");
        accReq.setOtp(otp);

        CustomerSignUpRequest req = new CustomerSignUpRequest();
        req.setFullName("Nguyen Van A");
        req.setAccount(accReq);

        // 3. Gọi service
        String token = authService.createCustomer(req);

        // 4. Kiểm tra kết quả
        Account saved = accountRepository.findByEmail(email);
        assertThat(saved).isNotNull();
        assertThat(saved.getUsername()).isEqualTo(username);
        assertThat(token).isNotEmpty();
    }
}
