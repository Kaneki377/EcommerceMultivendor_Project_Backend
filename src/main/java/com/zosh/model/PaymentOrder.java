package com.zosh.model;

import com.zosh.domain.PaymentMethod;
import com.zosh.domain.PaymentOrderStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
public class PaymentOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long amount;

    @Enumerated(EnumType.STRING)
    private PaymentOrderStatus status = PaymentOrderStatus.PENDING;

<<<<<<< Updated upstream
    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;

=======
>>>>>>> Stashed changes
    private String paymentLinkId;

    @ManyToOne
    private Customer customer;

    @OneToMany
    private Set<Order> orders = new HashSet<>();
}
