package com.zosh.service;

import com.razorpay.PaymentLink;
import com.razorpay.RazorpayException;
import com.stripe.exception.StripeException;
import com.zosh.model.Customer;
import com.zosh.model.Order;
import com.zosh.model.PaymentOrder;

import java.util.Set;

public interface PaymentService {

    PaymentOrder createOrder(Customer customer, Set<Order> orders);

    PaymentOrder getPaymentOrderById(Long orderId) throws Exception;

    PaymentOrder getPaymentOrderByPaymentId(String paymentLinkId) throws Exception;

    Boolean ProceedPaymentOrder(PaymentOrder paymentOrder, String paymentId, String paymentLinkId) throws RazorpayException;

    PaymentLink createRazorpayPaymentLink(Customer customer, Long amount,
                                          Long orderId) throws RazorpayException;

    String createStripePaymentLink(Customer customer,
                                   Long amount, Long orderId) throws StripeException;
}
