package com.zosh.service.impl;

import com.paypal.api.payments.*;
import com.paypal.api.payments.Payment;
import com.paypal.api.payments.Transaction;
import com.paypal.base.rest.APIContext;
import com.paypal.base.rest.PayPalRESTException;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import com.zosh.domain.PaymentOrderStatus;
import com.zosh.domain.PaymentStatus;
import com.zosh.model.*;
import com.zosh.model.Order;
import com.zosh.repository.OrderRepository;
import com.zosh.repository.PaymentOrderRepository;
import com.zosh.service.PaymentService;
import com.zosh.service.SellerReportService;
import com.zosh.service.SellerService;
import com.zosh.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentOrderRepository paymentOrderRepository;
    private final OrderRepository orderRepository;
    private final APIContext apiContext;
    private final TransactionService transactionService;
    private final SellerService sellerService;
    private final SellerReportService sellerReportService;

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
    public Boolean ProceedPaymentOrder(PaymentOrder paymentOrder, String paymentId, String paymentLinkId) throws StripeException {
        if (paymentOrder.getStatus().equals(PaymentOrderStatus.PENDING)) {

            PaymentIntent paymentIntent = PaymentIntent.retrieve(paymentId); // ID dạng pi_xxx

            String status = paymentIntent.getStatus(); // e.g. "succeeded"
            Long amountReceived = paymentIntent.getAmountReceived(); // số tiền nhận được

            if ("succeeded".equals(status)) {
                Set<Order> orders = paymentOrder.getOrders();
                for (Order order : orders) {
                    order.setPaymentStatus(PaymentStatus.COMPLETED);
                    orderRepository.save(order);
                }

                paymentOrder.setStatus(PaymentOrderStatus.SUCCESS);
                paymentOrderRepository.save(paymentOrder);
                return true;
            }

            paymentOrder.setStatus(PaymentOrderStatus.FAILED);
            paymentOrderRepository.save(paymentOrder);
            return false;
        }

        return false;
    }

    @Override
    public Session createStripePaymentLink(Customer customer, Long amount, Long orderId) throws StripeException {
        Stripe.apiKey = stripeSecretKey;

        SessionCreateParams params = SessionCreateParams.builder()
                .addPaymentMethodType(SessionCreateParams.PaymentMethodType.CARD)
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl("http://localhost:5173/payment-success/" + orderId + "?session_id={CHECKOUT_SESSION_ID}")
                .setCancelUrl("http://localhost:5173/payment/cancel")
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
                ).build();

        Session session = Session.create(params);

        //return session.getUrl();
        return session;
    }

    @Override
    public String createPaypalPaymentLink(Long amount, Long paymentOrderId) throws PayPalRESTException {
        // Convert từ VNĐ sang USD
        double exchangeRate = 25000.0;
        double amountInUSD = (double) amount / exchangeRate;

        Amount paymentAmount = new Amount();

        paymentAmount.setCurrency("USD");

        paymentAmount.setTotal(String.format("%.2f", amountInUSD));

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


        String cancelUrl = "http://localhost:5173/payment/cancel"; // URL của frontend
        String successUrl = "http://localhost:5173/payment/paypal/callback?paymentOrderId=" + paymentOrderId; // URL của backend

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

    @Override
    @Transactional // Đảm bảo tất cả các thao tác DB hoặc thành công hoặc rollback
    public void executeAndCompletePaypalOrder(Customer customer, String paymentId, String payerId, Long paymentOrderId) throws Exception {
        // 1. Tìm PaymentOrder
        PaymentOrder paymentOrder = getPaymentOrderById(paymentOrderId);

        // 2. Kiểm tra bảo mật: Đảm bảo người dùng đang thực hiện là chủ của đơn hàng
        if (!paymentOrder.getCustomer().getId().equals(customer.getId())) {
            throw new Exception("You are not authorized to complete this payment.");
        }

        try {
            // 3. Thực thi thanh toán với PayPal
            Payment payment = executePaypalPayment(paymentId, payerId);

            // 4. Kiểm tra kết quả
            if ("approved".equalsIgnoreCase(payment.getState())) {
                // 5. Xử lý logic khi thanh toán thành công
                paymentOrder.setPaymentLinkId(payment.getId()); // Lưu paymentId của PayPal
                paymentOrder.setStatus(PaymentOrderStatus.SUCCESS);
                paymentOrderRepository.save(paymentOrder);

                // Cập nhật trạng thái các Order con, tạo Transaction và SellerReport
                for (Order order : paymentOrder.getOrders()) {
                    order.setPaymentStatus(PaymentStatus.COMPLETED);
                    orderRepository.save(order);

                    // Tạo transaction
                    transactionService.createTransaction(order);

                    // Cập nhật report cho seller
                    Seller seller = sellerService.getSellerById(order.getSellerId());
                    SellerReport report = sellerReportService.getSellerReport(seller);
                    report.setTotalOrders(report.getTotalOrders() + 1);
                    report.setTotalEarnings(report.getTotalEarnings() + order.getTotalSellingPrice());
                    report.setTotalSales(report.getTotalSales() + order.getOrderItems().size());
                    sellerReportService.updateSellerReport(report);
                }
            } else {
                // Nếu trạng thái không phải "approved"
                throw new Exception("PayPal payment was not approved.");
            }
        } catch (PayPalRESTException e) {
            // 6. Xử lý khi có lỗi từ PayPal
            paymentOrder.setStatus(PaymentOrderStatus.FAILED);
            paymentOrderRepository.save(paymentOrder);
            // Ném lại lỗi để controller có thể xử lý
            throw new Exception("Failed to execute PayPal payment: " + e.getMessage());
        }
    }
}
