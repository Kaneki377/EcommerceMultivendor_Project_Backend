package com.zosh.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(name="uk_payout_item", columnNames={"payout_id","order_item_id"}),
        indexes = @Index(name="idx_payout_item_oi", columnList="order_item_id"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PayoutItem {

    @Id
    @GeneratedValue Long id;

    @ManyToOne(fetch=FetchType.LAZY)
    Payout payout;

    @ManyToOne(fetch=FetchType.LAZY)
    OrderItem orderItem;

    @Column(precision=18, scale=2)
    BigDecimal commissionAmount; // snapshot để tránh lệch
}
