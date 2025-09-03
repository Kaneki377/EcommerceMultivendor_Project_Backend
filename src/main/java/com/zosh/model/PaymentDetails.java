package com.zosh.model;

import com.zosh.domain.PaymentMethod;
import com.zosh.domain.PaymentStatus;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class PaymentDetails {

    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;

}
