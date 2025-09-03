package com.zosh.controller;

import com.zosh.domain.PaymentMethod;
import com.zosh.domain.PaymentOrderStatus;
import com.zosh.domain.PaymentStatus;
import com.zosh.model.*;
import com.zosh.repository.CartRepository;
import com.zosh.repository.OrderRepository;
import com.zosh.repository.PaymentOrderRepository;
import com.zosh.response.ApiResponse;
import com.zosh.response.PaymentLinkResponse;
import com.zosh.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/payment")
public class PaymentController {

    private final PaymentService paymentService;

    private final CustomerService customerService;

    private final SellerService sellerService;

    private final SellerReportService sellerReportService;

    private final TransactionService transactionService;
    private final PaymentOrderRepository paymentOrderRepository;
    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;


    @PostMapping("{paymentMethod}/order/{orderId}")
    public ResponseEntity<PaymentLinkResponse> paymentHandler(
            @PathVariable PaymentMethod paymentMethod,
            @PathVariable Long orderId,
            @RequestHeader("Authorization") String jwt) throws Exception {

        Customer customer = customerService.findCustomerProfileByJwt(jwt);

        PaymentLinkResponse paymentResponse;

        PaymentOrder order= paymentService.getPaymentOrderById(orderId);

        return new ResponseEntity<>(null, HttpStatus.CREATED);
    }


    @PostMapping("/stripe/verify")
    public ResponseEntity<?> verifyStripe(
            @RequestBody Map<String, String> body,
            @RequestHeader("Authorization") String jwt
    ) throws Exception {
        String sessionId = body.get("sessionId");
        Customer customer = customerService.findCustomerProfileByJwt(jwt);

        boolean isPaid = paymentService.verifyStripePayment(sessionId);
        if (!isPaid) {
            return ResponseEntity.status(400).body("Stripe Payment failed or not paid.");
        }

        PaymentOrder paymentOrder = paymentService.getPaymentOrderByPaymentId(sessionId);

        for (Order order : paymentOrder.getOrders()) {

            paymentService.completePaymentForOrder(order);
        }

        Cart cart = cartRepository.findByCustomerId(customer.getId());
        cart.setCouponPrice(0);
        cart.setCouponCode(null);
        cartRepository.save(cart);

        paymentOrder.setStatus(PaymentOrderStatus.SUCCESS);
        paymentOrderRepository.save(paymentOrder);

        return ResponseEntity.ok("Stripe Payment verified and processed.");
    }
}



