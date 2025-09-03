package com.zosh.service.impl;

import com.zosh.model.AffiliateCommission;
import com.zosh.model.AffiliateCommission.CommissionStatus;
import com.zosh.model.Order;
import com.zosh.model.OrderItem;
import com.zosh.repository.AffiliateCommissionRepository;
import com.zosh.service.AffiliateCommissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AffiliateCommissionServiceImpl implements AffiliateCommissionService {

    private final AffiliateCommissionRepository commissionRepository;

    @Override
    @Transactional
    public List<AffiliateCommission> createCommissionsForOrder(Order order) {
        List<AffiliateCommission> commissions = new ArrayList<>();

        for (OrderItem orderItem : order.getOrderItems()) {
            if (orderItem.getAffiliateLink() == null) continue;
            if (commissionRepository.existsByOrderItem_Id(orderItem.getId())) continue; // chống trùng

            AffiliateCommission c = new AffiliateCommission();
            c.setKoc(orderItem.getAffiliateLink().getKoc());
            c.setOrderItem(orderItem);
            c.setAffiliateLink(orderItem.getAffiliateLink());
            c.setCampaign(orderItem.getAffiliateLink().getCampaign());

            long itemValue = (long) orderItem.getSellingPrice() * orderItem.getQuantity(); // price/unit * qty
            Double pct = orderItem.getAffiliateLink().getCampaign().getCommissionPercent();
            long amount = Math.round(itemValue * pct / 100.0);

            c.setOrderValue(itemValue);
            c.setCommissionPercent(pct);
            c.setCommissionAmount(amount);
            c.setStatus(CommissionStatus.PENDING);

            commissions.add(c);
        }
        return commissions.isEmpty() ? commissions : commissionRepository.saveAll(commissions);
    }

    @Override
    @Transactional
    public void updateCommissionStatus(Long orderId, CommissionStatus status) {
        List<AffiliateCommission> commissions = commissionRepository.findByOrderId(orderId);

        for (AffiliateCommission commission : commissions) {
            commission.setStatus(status);
            if (status == CommissionStatus.PAID) {
                commission.setPaidAt(LocalDateTime.now());
            }
        }

        commissionRepository.saveAll(commissions);
    }

    @Override
    public List<AffiliateCommission> getKocCommissions(Long kocId) {
        return commissionRepository.findByKoc_Id(kocId);
    }

    @Override
    public Page<AffiliateCommission> getKocCommissions(Long kocId, Pageable pageable) {
        return commissionRepository.findByKoc_Id(kocId, pageable);
    }

    @Override
    public Long getTotalCommissionByStatus(Long kocId, CommissionStatus status) {
        Long total = commissionRepository.getTotalCommissionByKocAndStatus(kocId, status);
        return total != null ? total : 0L;
    }

    @Override
    public Map<String, Object> getKocDashboardData(Long kocId) {
        Map<String, Object> data = new HashMap<>();

        data.put("pendingCommission", getTotalCommissionByStatus(kocId, CommissionStatus.PENDING));
        data.put("confirmedCommission", getTotalCommissionByStatus(kocId, CommissionStatus.CONFIRMED));
        data.put("paidCommission", getTotalCommissionByStatus(kocId, CommissionStatus.PAID));
        data.put("totalCommission",
                getTotalCommissionByStatus(kocId, CommissionStatus.CONFIRMED) +
                        getTotalCommissionByStatus(kocId, CommissionStatus.PAID));

        return data;
    }

    @Override
    public List<AffiliateCommission> getCommissionsByDateRange(Long kocId, LocalDateTime startDate,
            LocalDateTime endDate) {
        return commissionRepository.findByKocAndDateRange(kocId, startDate, endDate);
    }

    @Override
    public List<Map<String, Object>> getMonthlyCommissionStats(Long kocId) {
        List<Object[]> results = commissionRepository.getMonthlyCommissionStats(kocId, CommissionStatus.CONFIRMED);

        return results.stream()
                .map(row -> {
                    Map<String, Object> stat = new HashMap<>();
                    stat.put("year", row[0]);
                    stat.put("month", row[1]);
                    stat.put("total", row[2]);
                    return stat;
                })
                .toList();
    }

    @Override
    @Transactional
    public void markCommissionAsPaid(List<Long> commissionIds) {
        List<AffiliateCommission> commissions = commissionRepository.findAllById(commissionIds);

        for (AffiliateCommission commission : commissions) {
            commission.setStatus(CommissionStatus.PAID);
            commission.setPaidAt(LocalDateTime.now());
        }

        commissionRepository.saveAll(commissions);
    }

    @Override
    public List<AffiliateCommission> getConfirmedCommissions() {
        return commissionRepository.findByStatus(CommissionStatus.CONFIRMED);
    }
}
