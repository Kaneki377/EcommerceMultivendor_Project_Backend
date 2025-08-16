package com.zosh.service.impl;

import com.zosh.model.Order;
import com.zosh.model.OrderItem;
import com.zosh.model.PaymentOrder;
import com.zosh.repository.OrderItemRepository;
import com.zosh.service.CommissionService;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CommissionServiceImpl implements CommissionService {

    private final OrderItemRepository orderItemRepository;

    @Override
    @Transactional
    public void snapshotForPaymentOrder(PaymentOrder paymentOrder) {
        for (Order o : paymentOrder.getOrders()) {
            for (OrderItem oi : o.getOrderItems()) {
                if (oi.getAffiliateLink() == null) continue;

                var cmp = oi.getAffiliateLink().getCampaign();
                double percent = cmp.getCommissionPercent() != null ? cmp.getCommissionPercent() : 0d;

                // sellingPrice hiện là "line total" (đã quantity) theo code của bạn
                BigDecimal lineTotal = BigDecimal.valueOf(oi.getSellingPrice());
                BigDecimal commission = lineTotal.multiply(BigDecimal.valueOf(percent)).divide(BigDecimal.valueOf(100));

                oi.setCommissionAmount(commission);
                // Cho phép payout ngay khi thanh toán hoàn tất (hoặc bạn có thể chờ DELIVERED)
                oi.setCommissionStatus(OrderItem.CommissionStatus.PAYABLE);
                if (oi.getAttributedAt() == null) oi.setAttributedAt(LocalDateTime.now());
                orderItemRepository.save(oi);
            }
        }
    }
}
