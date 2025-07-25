package com.zosh.service;

import com.paypal.api.payments.Payment;
import com.paypal.base.rest.PayPalRESTException;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.zosh.model.Customer;
import com.zosh.model.Order;
import com.zosh.model.PaymentOrder;

import java.util.Set;

public interface PaymentService {

    PaymentOrder createOrder(Customer customer, Set<Order> orders);

    PaymentOrder getPaymentOrderById(Long orderId) throws Exception;

    PaymentOrder getPaymentOrderByPaymentId(String paymentId) throws Exception;

    Session createStripePaymentLink(Customer customer,
                                    Long amount, Long orderId) throws StripeException;

    String createPaypalPaymentLink(Long amount, Long paymentOrderId) throws PayPalRESTException;

    Payment executePaypalPayment(String paymentId, String payerId) throws PayPalRESTException;
}
