package com.zosh.service.impl;

import com.razorpay.PaymentLink;
import com.zosh.model.Customer;
import com.zosh.model.Order;
import com.zosh.model.PaymentOrder;
import com.zosh.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {


    @Override
    public PaymentOrder createOrder(Customer customer, Set<Order> orders) {
        return null;
    }

    @Override
    public PaymentOrder getPaymentOrderById(String orderId) {
        return null;
    }

    @Override
    public PaymentOrder getPaymentOrderByPaymentId(String paymentId) {
        return null;
    }

    @Override
    public Boolean ProceedPaymentOrder(PaymentOrder paymentOrder, String paymentId, String paymentLinkId) {
        return null;
    }

    @Override
    public PaymentLink createRazorpayPaymentLink(Customer customer, Long amount, Long orderId) {
        return null;
    }

    @Override
    public String createStripePaymentLink(Customer customer, Long amount, Long orderId) {
        return "";
    }
}
