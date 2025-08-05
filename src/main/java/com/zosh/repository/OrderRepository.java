package com.zosh.repository;

import com.zosh.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByCustomerId(long customerId);

    List<Order> findBySellerId(long sellerId);

    List<Order> findBySellerIdOrderByOrderDateDesc(Long sellerId);

    List<Order> findBySellerIdAndOrderDateBetween(Long sellerId, LocalDateTime startDate, LocalDateTime endDate);
}
