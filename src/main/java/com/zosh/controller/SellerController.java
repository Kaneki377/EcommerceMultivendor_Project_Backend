package com.zosh.controller;

import com.zosh.config.JwtProvider;
import com.zosh.domain.AccountStatus;
import com.zosh.dto.SellerProfileDto;
import com.zosh.exceptions.SellerException;
import com.zosh.model.*;

import com.zosh.repository.VerificationCodeRepository;
import com.zosh.request.*;

import com.zosh.response.AuthResponse;
import com.zosh.service.*;
import com.zosh.utils.OtpUtils;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Date;
import java.util.List;
import java.util.Map;

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
        String verificationLink = "http://localhost:5173/verify-seller/";
        String  frontend_url = verificationLink + verificationCode.getOtp();
        String subject = "Zosh Bazaar Email Verification Code";
        String text = "Welcome to Zosh Bazaar, verify your account using this link , link will expire after 10 minutes !";

        emailService.sendVerificationOtpEmail(email, verificationCode.getOtp(), subject, text + frontend_url);

        return new ResponseEntity<>(savedSeller, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Seller> getSellerById(@PathVariable Long id) throws SellerException {
        Seller seller = sellerService.getSellerById(id);
        return new ResponseEntity<>(seller, HttpStatus.OK);
    }

    @GetMapping("/profile")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<SellerProfileDto> getSellerByJwt(
            @RequestHeader("Authorization") String jwt) throws SellerException {
        String username = jwtProvider.getUsernameFromJwtToken(jwt);
        Seller seller = sellerService.getSellerByUsername(username);
        return ResponseEntity.ok(SellerProfileDto.fromEntity(seller));
    }


    @GetMapping("/report")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<SellerReport> getSellerReport(
            @RequestHeader("Authorization") String jwt) throws Exception {
        String username = jwtProvider.getUsernameFromJwtToken(jwt);
        Seller seller = sellerService.getSellerByUsername(username);
        SellerReport report = sellerReportService.getSellerReport(seller);
        return new ResponseEntity<>(report, HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<Page<SellerProfileDto>> getAllSellers(
            @RequestParam(required = false) AccountStatus status,
            @RequestParam(defaultValue = "0") int page,      // 0-based
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort // "field,dir"
    ) {
        String[] parts = sort.split(",");
        Sort s = parts.length == 2 ? Sort.by(Sort.Direction.fromString(parts[1]), parts[0])
                : Sort.by(parts[0]);
        Pageable pageable = PageRequest.of(page, size, s);

        Page<Seller> result = sellerService.getAllSellers(status, pageable);

        // map Page<Seller> -> Page<SellerProfileDto>
        Page<SellerProfileDto> dtoPage = result.map(SellerProfileDto::fromEntity);

        return ResponseEntity.ok(dtoPage);
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
