package com.zosh.repository;

import com.zosh.model.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    @Query("""
      select oi from OrderItem oi
      join oi.order o
      where oi.affiliateLink is not null
        and oi.commissionStatus = com.zosh.model.OrderItem.CommissionStatus.PAYABLE
        and o.paymentStatus = com.zosh.domain.PaymentStatus.COMPLETED
        and oi.affiliateLink.koc.id = :kocId
    """)
    List<OrderItem> findPayableByKoc(@Param("kocId") Long kocId);

    @Query("""
      select oi from OrderItem oi
      join oi.order o
      where oi.affiliateLink is not null
        and oi.commissionStatus = com.zosh.model.OrderItem.CommissionStatus.PAYABLE
        and o.paymentStatus = com.zosh.domain.PaymentStatus.COMPLETED
        and o.sellerId = :sellerId
    """)
    List<OrderItem> findSellerPayable(@Param("sellerId") Long sellerId);

    @Query("""
      select oi from OrderItem oi
      join oi.order o
      where oi.id in :ids
        and o.sellerId = :sellerId
        and oi.affiliateLink is not null
        and oi.commissionStatus = com.zosh.model.OrderItem.CommissionStatus.PAYABLE
    """)
    List<OrderItem> findSellerPayableByIds(@Param("sellerId") Long sellerId, @Param("ids") List<Long> ids);
}
