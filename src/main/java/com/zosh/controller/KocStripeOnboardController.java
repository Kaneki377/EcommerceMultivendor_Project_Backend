package com.zosh.controller;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Account;
import com.stripe.model.AccountLink;
import com.stripe.param.AccountCreateParams;
import com.stripe.param.AccountLinkCreateParams;
import com.zosh.model.Koc;
import com.zosh.repository.KocRepository;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/stripe")

public class KocStripeOnboardController {

    private final KocRepository kocRepository;

    public KocStripeOnboardController(KocRepository kocRepository,
                                   @Value("${stripe.api.key}") String apiKey) {
        this.kocRepository = kocRepository;
        Stripe.apiKey = apiKey; // Set key ngay tại controller, không cần @Configuration riêng
    }

    @Value("${app.url.base}")
    private String appBaseUrl;
    @Value("${stripe.onboard.return:${app.url.base}/api/stripe/onboard/return}")
    private String returnUrl;
    @Value("${stripe.onboard.refresh:${app.url.base}/api/stripe/onboard/refresh}")
    private String refreshUrl;

    /** 1) Tạo/refresh link Onboarding và 302 redirect sang Stripe */
    @PostMapping("/onboard/{kocId}")
    public ResponseEntity<Void> startOnboard(@PathVariable Long kocId) {
        Koc koc = kocRepository.findById(kocId)
                .orElseThrow(() -> new RuntimeException("KOC not found"));

        try {
            // Tạo connected account nếu chưa có
            String accountId = koc.getStripeAccountId();
            if (accountId == null || accountId.isBlank()) {
                AccountCreateParams createParams = AccountCreateParams.builder()
                        .setType(AccountCreateParams.Type.EXPRESS)
                        .setCountry("US") // tuỳ quốc gia
                        .setEmail(koc.getCustomer().getAccount().getEmail())
                        .setCapabilities(
                                AccountCreateParams.Capabilities.builder()
                                        .setCardPayments(
                                                AccountCreateParams.Capabilities.CardPayments.builder()
                                                        .setRequested(true)
                                                        .build()
                                        )
                                        .setTransfers(
                                                AccountCreateParams.Capabilities.Transfers.builder()
                                                        .setRequested(true)
                                                        .build()
                                        )
                                        .build()
                        )
                        .build();
                Account acct = Account.create(createParams);
                accountId = acct.getId();
                koc.setStripeAccountId(accountId);
                kocRepository.save(koc);
            }

            // Tạo account link cho KYC
            AccountLinkCreateParams linkParams = AccountLinkCreateParams.builder()
                    .setAccount(accountId)
                    .setType(AccountLinkCreateParams.Type.ACCOUNT_ONBOARDING)
                    .setRefreshUrl(refreshUrl + "?kocId=" + kocId)
                    .setReturnUrl(returnUrl + "?kocId=" + kocId)
                    .build();
            AccountLink link = AccountLink.create(linkParams);

            HttpHeaders headers = new HttpHeaders();
            headers.setLocation(URI.create(link.getUrl()));
            return new ResponseEntity<>(headers, HttpStatus.SEE_OTHER); // 303/302 đều được
        } catch (StripeException e) {
            throw new RuntimeException("Stripe error: " + e.getMessage(), e);
        }
    }

    /** 2) Stripe trả về đây sau khi KOC hoàn tất/thoát; ta sync payouts_enabled */
    @GetMapping("/onboard/return")
    public ResponseEntity<String> onboardReturn(@RequestParam Long kocId) {
        Koc koc = kocRepository.findById(kocId)
                .orElseThrow(() -> new RuntimeException("KOC not found"));
        if (koc.getStripeAccountId() == null) {
            return ResponseEntity.badRequest().body("Missing stripeAccountId");
        }

        try {
            Account acct = Account.retrieve(koc.getStripeAccountId());
            boolean enabled = Boolean.TRUE.equals(acct.getPayoutsEnabled());
            koc.setStripePayoutsEnabled(enabled);
            if (enabled && koc.getStripeOnboardedAt() == null) {
                koc.setStripeOnboardedAt(LocalDateTime.now());
            }
            kocRepository.save(koc);

            return ResponseEntity.ok(
                    enabled ? "Onboarding thành công! payouts_enabled = true"
                            : "Onboarding chưa hoàn tất. Vui lòng tiếp tục trên Stripe."
            );
        } catch (StripeException e) {
            throw new RuntimeException("Stripe error: " + e.getMessage(), e);
        }
    }

    /** (Optional) Nếu KOC bấm back, Stripe sẽ gọi về đây → tạo link mới rồi redirect tiếp */
    @GetMapping("/onboard/refresh")
    public ResponseEntity<Void> onboardRefresh(@RequestParam Long kocId) {
        // tái sử dụng endpoint POST cho gọn
        return startOnboard(kocId);
    }
}
