package com.zosh.service;

import com.stripe.exception.StripeException;
import com.stripe.model.Account;
import com.stripe.model.AccountLink;
import com.stripe.param.AccountCreateParams;
import com.stripe.param.AccountLinkCreateParams;
import com.zosh.exceptions.KocException;
import com.zosh.model.Koc;
import com.zosh.repository.KocRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class KocStripeOnboardService {

    private final KocRepository kocRepository;

    @Value("${stripe.onboard.return}")
    private String returnUrl;

    @Value("${stripe.onboard.refresh}")
    private String refreshUrl;

    /** Tạo hoặc lấy account Stripe cho KOC, rồi sinh Account Link (URL KYC) */
    @Transactional
    public String createOrRefreshOnboardLink(Long kocId) {
        Koc koc = kocRepository.findById(kocId)
                .orElseThrow(() -> new KocException("KOC not found"));

        String accountId = koc.getStripeAccountId();
        try {
            if (accountId == null || accountId.isBlank()) {
                // 1) Tạo connected account (Express)
                AccountCreateParams params = AccountCreateParams.builder()
                        .setType(AccountCreateParams.Type.EXPRESS)
                        .setCountry("VN") // hoặc quốc gia phù hợp
                        .setEmail(koc.getCustomer().getAccount().getEmail())
                        .build();
                Account account = Account.create(params);
                accountId = account.getId();           // "acct_xxx"
                koc.setStripeAccountId(accountId);
                kocRepository.save(koc);
            }

            // 2) Sinh account link để KOC điền KYC
            AccountLinkCreateParams linkParams = AccountLinkCreateParams.builder()
                    .setAccount(accountId)
                    .setRefreshUrl(refreshUrl + "?kocId=" + kocId)
                    .setReturnUrl(returnUrl + "?kocId=" + kocId)
                    .setType(AccountLinkCreateParams.Type.ACCOUNT_ONBOARDING)
                    .build();

            AccountLink link = AccountLink.create(linkParams);
            return link.getUrl(); // redirect KOC sang URL này

        } catch (StripeException e) {
            throw new RuntimeException("Stripe error: " + e.getMessage(), e);
        }
    }

    /** Đồng bộ trạng thái payouts_enabled sau khi KOC quay lại từ Stripe */
    @Transactional
    public Koc syncStatus(Long kocId) {
        Koc koc = kocRepository.findById(kocId)
                .orElseThrow(() -> new KocException("KOC not found"));

        if (koc.getStripeAccountId() == null) return koc;

        try {
            Account acct = Account.retrieve(koc.getStripeAccountId());
            boolean enabled = Boolean.TRUE.equals(acct.getPayoutsEnabled());
            koc.setStripePayoutsEnabled(enabled);
            if (enabled && koc.getStripeOnboardedAt() == null) {
                koc.setStripeOnboardedAt(LocalDateTime.now());
            }
            return kocRepository.save(koc);
        } catch (StripeException e) {
            throw new RuntimeException("Stripe error: " + e.getMessage(), e);
        }
    }
}
