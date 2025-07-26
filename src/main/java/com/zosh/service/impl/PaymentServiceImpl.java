package com.zosh.service.impl;

import com.paypal.api.payments.*;
import com.paypal.api.payments.Payment;
import com.paypal.base.rest.APIContext;
import com.paypal.base.rest.PayPalRESTException;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import com.zosh.model.Customer;
import com.zosh.model.Order;
import com.zosh.model.PaymentOrder;
import com.zosh.repository.PaymentOrderRepository;
import com.zosh.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.util.Arrays;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentOrderRepository paymentOrderRepository;

    private final APIContext apiContext;

    @Value("${stripe.api.key}")
    private String stripeSecretKey;

    @Override
    public PaymentOrder createOrder(Customer customer, Set<Order> orders) {
        Long amount = orders.stream().mapToLong(Order::getTotalSellingPrice).sum();

        PaymentOrder paymentOrder = new PaymentOrder();
        paymentOrder.setAmount(amount);
        paymentOrder.setCustomer(customer);
        paymentOrder.setOrders(orders);

        return paymentOrderRepository.save(paymentOrder);
    }

    @Override
    public PaymentOrder getPaymentOrderById(Long orderId) throws Exception {

        return paymentOrderRepository.findById(orderId).orElseThrow(()->
                new Exception("Payment order not found"));
    }

    @Override
    public PaymentOrder getPaymentOrderByPaymentId(String paymentId) throws Exception {
        PaymentOrder paymentOrder = paymentOrderRepository
                .findByPaymentLinkId(paymentId);

        if(paymentOrder==null){
            throw new Exception("payment order not found with id "+paymentId);
        }
        return paymentOrder;
    }

    @Override
    public Session createStripePaymentLink(Customer customer, Long amount, Long orderId) throws StripeException {
        Stripe.apiKey = stripeSecretKey;

        SessionCreateParams params = SessionCreateParams.builder()
                .addPaymentMethodType(SessionCreateParams.PaymentMethodType.CARD)
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl("http://localhost:3000/payment-success/"+orderId)
                .setCancelUrl("http://localhost:3000/payment/cancel")
                .addLineItem(SessionCreateParams.LineItem.builder()
                        .setQuantity(1L)
                        .setPriceData(SessionCreateParams.LineItem.PriceData.builder()
                                .setCurrency("usd")
                                .setUnitAmount(amount*100)
                                .setProductData(
                                        SessionCreateParams
                                                .LineItem.PriceData.ProductData
                                        .builder().setName("Top up wallet")
                                        .build()
                                ).build()
                        ).build()
                ).build(); //Can set PAypal instead of CARD if implement PAPAL Payment

        Session session = Session.create(params);

        //return session.getUrl();
        return session;
    }

    @Override
    public String createPaypalPaymentLink(Long amount, Long paymentOrderId) throws PayPalRESTException {
        Amount paymentAmount = new Amount();
        paymentAmount.setCurrency("USD"); // Hoặc bạn có thể truyền currency vào
        // PayPal yêu cầu định dạng số thập phân, không nhân 100 như Razorpay
        paymentAmount.setTotal(String.format("%.2f", (double) amount));

        Transaction transaction = new Transaction();
        transaction.setAmount(paymentAmount);
        transaction.setDescription("Payment for order #" + paymentOrderId);

        Payment payment = getPayment(paymentOrderId, transaction);

        com.paypal.api.payments.Payment createdPayment = payment.create(apiContext);

        for (Links link : createdPayment.getLinks()) {
            if ("approval_url".equalsIgnoreCase(link.getRel())) {
                return link.getHref();
            }
        }

        throw new PayPalRESTException("Approval URL not found");
    }

    private static Payment getPayment(Long paymentOrderId, Transaction transaction) {
        Payer payer = new Payer();
        payer.setPaymentMethod("paypal");

        Payment payment = new Payment();
        payment.setIntent("sale");
        payment.setPayer(payer);
        payment.setTransactions(Arrays.asList(transaction));

        // Quan trọng: Xây dựng URL callback với paymentOrderId
        String cancelUrl = "http://localhost:3000/payment/cancel"; // URL của frontend
        String successUrl = "http://localhost:8080/api/payment/paypal/success?paymentOrderId=" + paymentOrderId; // URL của backend

        RedirectUrls redirectUrls = new RedirectUrls();
        redirectUrls.setCancelUrl(cancelUrl);
        redirectUrls.setReturnUrl(successUrl);
        payment.setRedirectUrls(redirectUrls);
        return payment;
    }

    @Override
    public Payment executePaypalPayment(String paymentId, String payerId) throws PayPalRESTException {
        Payment payment = new Payment();
        payment.setId(paymentId);
        PaymentExecution paymentExecution = new PaymentExecution();
        paymentExecution.setPayerId(payerId);
        return payment.execute(apiContext, paymentExecution);
    }
}
