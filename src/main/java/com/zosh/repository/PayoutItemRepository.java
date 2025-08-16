package com.zosh.repository;

import com.zosh.model.PayoutItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PayoutItemRepository extends JpaRepository<PayoutItem, Long> {

    boolean existsByOrderItem_Id(Long orderItemId);
}
