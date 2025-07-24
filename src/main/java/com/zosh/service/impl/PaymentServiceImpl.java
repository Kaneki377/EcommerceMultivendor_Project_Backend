package com.zosh.service.impl;

import com.razorpay.PaymentLink;
import com.zosh.model.Customer;
import com.zosh.model.Order;
import com.zosh.model.PaymentOrder;
import com.zosh.repository.OrderRepository;
import com.zosh.repository.PaymentOrderRepository;
import com.zosh.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentOrderRepository paymentOrderRepository;

    private final OrderRepository orderRepository;

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
    public PaymentOrder getPaymentOrderByPaymentId(String paymentLinkId) throws Exception {

        PaymentOrder paymentOrder = paymentOrderRepository.findByPaymentLinkId(paymentLinkId);
        if (paymentOrder == null) {
            throw new Exception("Payment order not found with payment link id");
        }
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
