package com.zosh.service;

import com.paypal.api.payments.Payment;
import com.paypal.base.rest.PayPalRESTException;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.zosh.exceptions.OrderException;
import com.zosh.model.Customer;
import com.zosh.model.Order;
import com.zosh.model.PaymentOrder;
import com.zosh.response.PaymentLinkResponse;

import java.util.Set;

public interface PaymentService {

    PaymentOrder createOrder(Customer customer, Set<Order> orders);

    PaymentOrder getPaymentOrderById(Long orderId) throws Exception;

    PaymentOrder getPaymentOrderByPaymentId(String paymentId) throws Exception;

    Boolean verifyStripePayment(String sessionId) throws StripeException, OrderException;

    PaymentLinkResponse createStripePaymentLink(Customer customer,
                                                 Long amount, Long orderId) throws StripeException;

    String createPaypalPaymentLink(Long amount, Long paymentOrderId) throws PayPalRESTException;

    Payment executePaypalPayment(String paymentId, String payerId) throws PayPalRESTException;

    void executeAndCompletePaypalOrder(Customer customer, String paymentId, String payerId, Long paymentOrderId) throws Exception;

    //Complete Order for COD
    void completePaymentForOrder(Order order) throws Exception;

    //Subtract quantity in stock
    void onPaymentSuccess(PaymentOrder paymentOrder) throws OrderException;
}
