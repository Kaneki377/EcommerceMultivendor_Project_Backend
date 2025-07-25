package com.zosh.controller;

import com.zosh.config.JwtProvider;
import com.zosh.domain.AccountStatus;
import com.zosh.exceptions.SellerException;
import com.zosh.model.*;

import com.zosh.repository.VerificationCodeRepository;
import com.zosh.request.*;

import com.zosh.response.AuthResponse;
import com.zosh.service.*;
import com.zosh.utils.OtpUtils;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/sellers")
@RequiredArgsConstructor
public class SellerController {

    private final SellerService sellerService;
    private final EmailService emailService;
    private final VerificationService verificationService;
    private final VerificationCodeRepository verificationCodeRepository;
    private final JwtProvider jwtProvider;
    private final AuthService authService;
    private final SellerReportService sellerReportService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> loginSeller(@Valid @RequestBody LoginRequest req) throws Exception {
        AuthResponse authResponse = authService.signIn(req);
        return ResponseEntity.ok(authResponse);
    }

    @PatchMapping("/verify/{otp}")
    public ResponseEntity<Seller> verifySellerEmail(
            @PathVariable String otp) throws Exception {


        VerificationCode verificationCode = verificationCodeRepository.findByOtp(otp);

        if (verificationCode == null || !verificationCode.getOtp().equals(otp)) {
            throw new SellerException("wrong otp...");
        }
        // Kiểm tra xem OTP đã hết hạn chưa
        if (verificationCode.getExpiresAt().before(new Date())) {
            verificationCodeRepository.delete(verificationCode); // Xoá luôn nếu hết hạn
            throw new SellerException("OTP is expired...");
        }
        Seller seller = sellerService.verifyEmail(verificationCode.getUsername(), otp);
        verificationCodeRepository.delete(verificationCode);
        return new ResponseEntity<>(seller, HttpStatus.OK);
    }


    @PostMapping
    public ResponseEntity<Seller> createSeller(@Valid @RequestBody SellerSignUpRequest req) throws Exception {
        // lưu seller vào DB
        Seller savedSeller = sellerService.createSeller(req);

        String otp = OtpUtils.generateOTP();
        String username = savedSeller.getAccount().getUsername();
        String email = savedSeller.getAccount().getEmail();

        VerificationCode verificationCode = verificationService.createVerificationCode(otp,username,email);

        String subject = "Zosh Bazaar Email Verification Code";
        String text = "Welcome to Zosh Bazaar, verify your account using this link , link will expire after 10 minutes !";
        String frontend_url = "http://localhost:5454/verify-seller/";
        emailService.sendVerificationOtpEmail(email, verificationCode.getOtp(), subject, text + frontend_url);

        return new ResponseEntity<>(savedSeller, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Seller> getSellerById(@PathVariable Long id) throws Exception {
        Seller seller = sellerService.getSellerById(id);
        return new ResponseEntity<>(seller, HttpStatus.OK);
    }

    @GetMapping("/profile")
    public ResponseEntity<Seller> getSellerByJwt(
            @RequestHeader("Authorization") String jwt) throws Exception {
        //String email = jwtProvider.getEmailFromJwtToken(jwt);
        //Seller seller = sellerService.getSellerByEmail(email);
        Seller seller = sellerService.getSellerProfile(jwt);
        return new ResponseEntity<>(seller, HttpStatus.OK);
    }

    @GetMapping("/report")
    public ResponseEntity<SellerReport> getSellerReport(
            @RequestHeader("Authorization") String jwt) throws Exception {
        Seller seller = sellerService.getSellerProfile(jwt);
        SellerReport report = sellerReportService.getSellerReport(seller);
        return new ResponseEntity<>(report, HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<List<Seller>> getAllSellers(
            @RequestParam(required = false) AccountStatus status) {
        List<Seller> sellers = sellerService.getAllSellers(status);
        return ResponseEntity.ok(sellers);
    }

    @PatchMapping()
    public ResponseEntity<Seller> updateSeller(
            @RequestHeader("Authorization") String jwt,
            @RequestBody Seller seller) throws Exception {

        Seller profile = sellerService.getSellerProfile(jwt);
        Seller updatedSeller = sellerService.updateSeller(profile.getId(), seller);
        return ResponseEntity.ok(updatedSeller);

    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSeller(@PathVariable Long id) throws SellerException {

        sellerService.deleteSeller(id);
        return ResponseEntity.noContent().build();

    }
}
