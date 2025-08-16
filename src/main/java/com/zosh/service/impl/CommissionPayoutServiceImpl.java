package com.zosh.service.impl;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Transfer;
import com.stripe.net.RequestOptions;
import com.zosh.config.JwtProvider;
import com.zosh.domain.PayoutStatus;
import com.zosh.dto.CommissionDTO;
import com.zosh.model.*;
import com.zosh.repository.OrderItemRepository;
import com.zosh.repository.PayoutItemRepository;
import com.zosh.repository.PayoutRepository;
import com.zosh.response.PayoutSummaryResponse;
import com.zosh.service.CommissionPayoutService;
import com.zosh.service.SellerService;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommissionPayoutServiceImpl implements CommissionPayoutService {

    private final JwtProvider jwtProvider;
    private final SellerService sellerService;
    private final OrderItemRepository orderItemRepository;
    private final PayoutRepository payoutRepository;
    private final PayoutItemRepository payoutItemRepository;

    @Value("${stripe.api.key}")
    private String stripeSecretKey;

    // Tỉ giá tạm (giống phần thanh toán)
    @Value("${app.exchangeRateVndUsd:25000}")
    private double exchangeRateVndUsd;

    @Override
    @Transactional(readOnly = true)
    public List<CommissionDTO> sellerListPayoutRequests(String jwt) {
        // Hiển thị các line PAYABLE (chưa nằm trong PayoutItem)
        var seller = sellerFromJwt(jwt);
        List<OrderItem> items = orderItemRepository.findSellerPayable(seller.getId()).stream()
                .filter(oi -> !payoutItemRepository.existsByOrderItem_Id(oi.getId()))
                .toList();

        return items.stream().map(this::toDTO).toList();
    }

    @Override
    @Transactional
    public List<CommissionDTO> sellerApprovePayout(String jwt, List<Long> orderItemIds, String transactionId, String note) {
        var seller = sellerFromJwt(jwt);
        List<OrderItem> items = orderItemRepository.findSellerPayableByIds(seller.getId(), orderItemIds).stream()
                .filter(oi -> !payoutItemRepository.existsByOrderItem_Id(oi.getId()))
                .toList();

        // Nhóm theo (KOC, Campaign) → mỗi nhóm tạo 1 Payout và 1 lần chuyển tiền Stripe
        Map<String, List<OrderItem>> groups = items.stream().collect(Collectors.groupingBy(
                oi -> oi.getAffiliateLink().getKoc().getId() + "-" + oi.getAffiliateLink().getCampaign().getId()
        ));

        List<CommissionDTO> result = new ArrayList<>();

        Stripe.apiKey = stripeSecretKey;

        for (var entry : groups.entrySet()) {
            List<OrderItem> group = entry.getValue();
            Koc koc = group.get(0).getAffiliateLink().getKoc();
            AffiliateCampaign campaign = group.get(0).getAffiliateLink().getCampaign();

            // Tổng hoa hồng nhóm
            BigDecimal gross = group.stream().map(OrderItem::getCommissionAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // Tạo payout bản ghi
            Payout payout = new Payout();
            payout.setKoc(koc);
            payout.setCampaign(campaign);
            payout.setGrossAmount(gross);
            payout.setRefundAdjustment(BigDecimal.ZERO);
            payout.setNetAmount(gross); // có thể trừ phí nền tảng tại đây
            payout.setStatus(PayoutStatus.PROCESSING);
            payout.setCreatedAt(java.time.LocalDateTime.now());
            payout = payoutRepository.save(payout);

            // Tạo transfer Stripe (Stripe Connect → chuyển tiền vào tài khoản KOC)
            String transferId = null;
            try {
                // VND → USD → cents
                long cents = Math.max(1L, Math.round((payout.getNetAmount().doubleValue() / exchangeRateVndUsd) * 100));

                // ✅ Cần KOC.stripeAccountId (acct_xxx) đã on-board Connect
                if (koc.getStripeAccountId() == null) {
                    throw new IllegalStateException("KOC missing stripeAccountId");
                }

                if (Boolean.FALSE.equals(koc.getStripePayoutsEnabled())) {
                    throw new IllegalStateException("KOC payouts are not enabled yet");
                }

                Map<String, Object> params = new HashMap<>();
                params.put("amount", cents);
                params.put("currency", "usd");
                params.put("destination", koc.getStripeAccountId());
                params.put("description", "Affiliate payout KOC#" + koc.getId() + " cmp#" + campaign.getId());

                RequestOptions requestOptions = RequestOptions.builder()
                        .setIdempotencyKey("payout-" + payout.getId()) // tránh double transfer
                        .build();

                Transfer transfer = Transfer.create(params, requestOptions);
                transferId = transfer.getId();

                payout.setStatus(PayoutStatus.PAID);
                payout.setExternalRef(transferId);
                payout.setPaidAt(java.time.LocalDateTime.now());
                payoutRepository.save(payout);

                // Gán PayoutItem và set trạng thái line → PAID
                for (OrderItem oi : group) {
                    PayoutItem pi = new PayoutItem(null, payout, oi, oi.getCommissionAmount());
                    payoutItemRepository.save(pi);
                    oi.setCommissionStatus(OrderItem.CommissionStatus.PAID);
                    orderItemRepository.save(oi);
                    result.add(toDTO(oi));
                }

            } catch (StripeException | RuntimeException ex) {
                // Fail: mark payout FAILED, giữ line ở PAYABLE để xử lý lại
                payout.setStatus(PayoutStatus.FAILED);
                payout.setExternalRef(transferId);
                payoutRepository.save(payout);
                // (tuỳ) log + ném lỗi hợp lệ
                throw new RuntimeException("Stripe payout failed: " + ex.getMessage(), ex);
            }
        }
        return result;
    }

    @Override
    @Transactional
    public List<CommissionDTO> sellerRejectPayout(String jwt, List<Long> orderItemIds, String note) {
        var seller = sellerFromJwt(jwt);
        // Với “reject”, mình không tạo Payout; chỉ reset trạng thái nếu lỡ bấm nhầm
        List<OrderItem> items = orderItemRepository.findSellerPayableByIds(seller.getId(), orderItemIds);
        // Không thay đổi gì (PAYABLE) hoặc set REVERSED nếu bạn muốn chặn payout
        items.forEach(oi -> oi.setCommissionStatus(OrderItem.CommissionStatus.PAYABLE));
        orderItemRepository.saveAll(items);
        return items.stream().map(this::toDTO).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PayoutSummaryResponse sellerPayoutSummary(String jwt) {
        var seller = sellerFromJwt(jwt);

        var paid = payoutRepository.findByCampaign_Seller_IdAndStatus(seller.getId(), PayoutStatus.PAID);
        var processing = payoutRepository.findByCampaign_Seller_IdAndStatus(seller.getId(), PayoutStatus.PROCESSING);
        var failed = payoutRepository.findByCampaign_Seller_IdAndStatus(seller.getId(), PayoutStatus.FAILED);

        BigDecimal totalPaid = paid.stream().map(Payout::getNetAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalPending = processing.stream().map(Payout::getNetAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalRequested = totalPaid.add(totalPending); // hoặc thêm CREATED nếu bạn dùng

        return PayoutSummaryResponse.builder()
                .totalRequested(totalRequested)
                .totalPaid(totalPaid)
                .totalPending(totalPending)
                .build();
    }

    @Override
    @Transactional
    public void kocRequestPayout(String jwt, List<Long> orderItemIds, boolean allEligible) {
        // tuỳ bạn có thêm endpoint cho KOC hay không
        // Ở bản tối giản, KOC có thể không cần request – Seller có thể duyệt trực tiếp các line PAYABLE.
        // Nếu muốn, bạn có thể: gom line PAYABLE của KOC → tạo Payout (status CREATED) để chờ SELLER approve.
        // (Bỏ qua chi tiết ở đây cho ngắn gọn)
    }

    // ================= helpers =================
    private Seller
    sellerFromJwt(String jwt) {
        String username = jwtProvider.getUsernameFromJwtToken(jwt);
        try {
            return sellerService.getSellerByUsername(username);
        } catch (Exception e) {
            throw new RuntimeException("Seller not found for token");
        }
    }

    private CommissionDTO toDTO(OrderItem oi) {
        return CommissionDTO.builder()
                .orderItemId(oi.getId())
                .orderId(oi.getOrder().getId())
                .productId(oi.getProduct().getId())
                .campaignId(oi.getAffiliateLink().getCampaign().getId())
                .kocId(oi.getAffiliateLink().getKoc().getId())
                .commissionAmount(oi.getCommissionAmount())
                .status(oi.getCommissionStatus())
                .attributedAt(oi.getAttributedAt())
                .build();
    }
}
