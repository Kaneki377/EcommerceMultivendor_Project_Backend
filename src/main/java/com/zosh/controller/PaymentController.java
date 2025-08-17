package com.zosh.controller;

import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
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
    private final CommissionService commissionService;


    @PostMapping("{paymentMethod}/order/{paymentOrderId}")
    public ResponseEntity<PaymentLinkResponse> paymentHandler(
            @PathVariable PaymentMethod paymentMethod,
            @PathVariable("paymentOrderId") Long paymentOrderId,
            @RequestHeader("Authorization") String jwt) throws Exception {

        Customer customer = customerService.findCustomerProfileByJwt(jwt);

        if (paymentMethod != PaymentMethod.STRIPE) {
            return ResponseEntity.badRequest().build();
        }

        PaymentLinkResponse resp = paymentService
                .initStripePaymentAndPersist(paymentOrderId, customer);

        return new ResponseEntity<>(resp, HttpStatus.CREATED);
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
<<<<<<< Updated upstream
        if (paymentOrder == null) {
            return ResponseEntity.status(404).body("PaymentOrder not found for sessionId");
        }


        try {
            Session session = Session.retrieve(sessionId);

            if (paymentOrder.getPaymentLinkId() == null) {
                paymentOrder.setPaymentLinkId(session.getId());
            }
            if (paymentOrder.getAmount() == null || paymentOrder.getAmount() <= 0) {
                Long amountMinor = session.getAmountTotal() != null ? session.getAmountTotal() : 0L;
                paymentOrder.setAmount(amountMinor);
            }
            if (paymentOrder.getPaymentMethod() == null) {
                paymentOrder.setPaymentMethod(PaymentMethod.STRIPE);
            }
            if (paymentOrder.getCustomer() == null) {
                paymentOrder.setCustomer(customer);
            }
            paymentOrder.setStatus(PaymentOrderStatus.PENDING); // để chắc chắn trạng thái hợp lệ trước khi finalize
            paymentOrderRepository.save(paymentOrder);
        } catch (StripeException e) {
            return ResponseEntity.status(500).body("Stripe retrieve session failed: " + e.getMessage());
        }

        commissionService.snapshotForPaymentOrder(paymentOrder);
=======
>>>>>>> Stashed changes

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



