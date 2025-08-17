package com.zosh.model;

import com.zosh.domain.OrderStatus;
import com.zosh.domain.PaymentMethod;
import com.zosh.domain.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String orderId;

    @ManyToOne
    private Customer customer;

    private Long sellerId;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> orderItems = new ArrayList<>();

    @ManyToOne
    private Address shippingAddress;

    @Embedded
    private PaymentDetails paymentDetails = new PaymentDetails();

    private double totalMrpPrice;

    private Integer totalSellingPrice;

//    private Integer discount;

    @Enumerated(EnumType.STRING)
    private OrderStatus orderStatus;

    private int totalItem;

    @Enumerated(EnumType.STRING)
//    @Enumerated(EnumType.ORDINAL) // Map enum thành số (ordinal) trong DB
    @Column(name = "payment_status")
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;

    private LocalDateTime orderDate = LocalDateTime.now();
    private LocalDateTime packedDate;
    private LocalDateTime deliverDate;


    @Column(name = "net_paid_amount", precision = 18, scale = 2)
    private BigDecimal netPaidAmount; // tổng sau chiết khấu nền tảng + seller + thuế + phí

    @PrePersist
    public void prePersist() {
        if (this.orderId == null) {
            this.orderId = OrderIdGenerator.generateOrderId();
        }
    }

}
