package com.zosh.controller;

import com.zosh.domain.USER_ROLE;
import com.zosh.exceptions.CustomerException;
import com.zosh.model.Customer;

import com.zosh.model.VerificationCode;
import com.zosh.repository.CustomerRepository;
import com.zosh.request.CustomerSignUpRequest;
import com.zosh.request.LoginRequest;
import com.zosh.request.SignUpRequest;
import com.zosh.response.ApiResponse;
import com.zosh.response.AuthResponse;
import com.zosh.service.AuthService;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final CustomerRepository customerRepository;
    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> createCustomerHandler(@Valid @RequestBody CustomerSignUpRequest req) throws CustomerException {
        //Tạo Customer bằng jwt token
        String jwt = authService.createUser(req);

        AuthResponse res = new  AuthResponse();
        res.setJwt(jwt);
        res.setMessage("Register successfully !");
        res.setRole(USER_ROLE.ROLE_CUSTOMER);
        return ResponseEntity.ok(res);
    }

    @PostMapping("/sent/login-signup-otp")
    public ResponseEntity<ApiResponse> sentOtpHandler(
            @RequestBody VerificationCode req) throws Exception {


        authService.sentSignUpOtp(req.getEmail());

        ApiResponse res = new ApiResponse();

        res.setMessage("Otp sent successfully !");


        return ResponseEntity.ok(res);
    }

    @PostMapping("/signin")
    public ResponseEntity<AuthResponse> loginHandler(
            @Valid @RequestBody LoginRequest req) throws Exception {


        AuthResponse authResponse = authService.signIn(req);

        return ResponseEntity.ok(authResponse);
    }
    @PostMapping("sellers/login")
    public ResponseEntity<AuthResponse> loginSeller(@Valid @RequestBody LoginRequest req) throws Exception {
        AuthResponse authResponse = authService.loginSeller(req);
        authResponse.setMessage("Welcome Seller!");
        return ResponseEntity.ok(authResponse);
    }
}
