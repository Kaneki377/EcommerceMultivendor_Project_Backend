package com.zosh.repository;

import com.zosh.model.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    @Query("""
      select count(oi) > 0
      from OrderItem oi
      where oi.order.customer.id = :customerId
        and oi.product.id = :productId
        and oi.order.orderStatus = com.zosh.domain.OrderStatus.DELIVERED
    """)
    boolean customerArrivedOrderForProduct(Long customerId, Long productId);
}
