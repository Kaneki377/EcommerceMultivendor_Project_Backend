package com.zosh.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.zosh.domain.OrderStatus;
import com.zosh.domain.PaymentMethod;
import com.zosh.domain.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String orderId;

    @ManyToOne
    private Customer customer;

    @Column(name = "shop_id")
    private Long sellerId;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> orderItems = new ArrayList<>();

    @ManyToOne
    private Address shippingAddress;

    @Embedded
    private PaymentDetails paymentDetails = new PaymentDetails();

    private double totalMrpPrice;

    private Integer totalSellingPrice;

    // private Integer discount;

    @Enumerated(EnumType.STRING)
    private OrderStatus orderStatus;

    private int totalItem;

    @Enumerated(EnumType.STRING)
    // @Enumerated(EnumType.ORDINAL) // Map enum thành số (ordinal) trong DB
    @Column(name = "payment_status")
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;

    private LocalDateTime orderDate = LocalDateTime.now();
    private LocalDateTime packedDate;
    private LocalDateTime deliverDate;

    @PrePersist
    public void prePersist() {
        if (this.orderId == null) {
            this.orderId = OrderIdGenerator.generateOrderId();
        }
        // Đảm bảo PaymentDetails không null
        if (this.paymentDetails == null) {
            this.paymentDetails = new PaymentDetails();
        }
    }

    // Custom getter để đảm bảo PaymentDetails không null
    public PaymentDetails getPaymentDetails() {
        if (this.paymentDetails == null) {
            this.paymentDetails = new PaymentDetails();
        }
        return this.paymentDetails;
    }
}
