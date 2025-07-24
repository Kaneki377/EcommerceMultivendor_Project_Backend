package com.zosh.service;

import com.razorpay.PaymentLink;
import com.zosh.model.Customer;
import com.zosh.model.Order;
import com.zosh.model.PaymentOrder;

import java.util.Set;

public interface PaymentService {

    PaymentOrder createOrder(Customer customer, Set<Order> orders);

    PaymentOrder getPaymentOrderById(Long orderId) throws Exception;

    PaymentOrder getPaymentOrderByPaymentId(String paymentLinkId) throws Exception;

    Boolean ProceedPaymentOrder(PaymentOrder paymentOrder, String paymentId, String paymentLinkId);

    PaymentLink createRazorpayPaymentLink(Customer customer, Long amount,
                                          Long orderId);

    String createStripePaymentLink(Customer customer,
                                   Long amount, Long orderId);
}
