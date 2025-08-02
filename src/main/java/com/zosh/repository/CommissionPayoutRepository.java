package com.zosh.repository;

import com.zosh.model.CommissionPayout;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface CommissionPayoutRepository extends JpaRepository<CommissionPayout,Long> {
    // Tìm payout theo kocId
    List<CommissionPayout> findByKocId(Long kocId);

    // Tìm payout theo trạng thái
    List<CommissionPayout> findByStatus(CommissionPayout.PayoutStatus status);

    // Tìm payout trong khoảng thời gian
    List<CommissionPayout> findByPayoutDateBetween(LocalDateTime start, LocalDateTime end);
}
