package com.zosh.controller;

import com.zosh.domain.PaymentMethod;
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

        Customer customer = customerService.findCustomerByJwtToken(jwt);

        PaymentLinkResponse paymentResponse;

        PaymentOrder order= paymentService.getPaymentOrderById(orderId);

        return new ResponseEntity<>(null, HttpStatus.CREATED);
    }


    @GetMapping("{paymentId}")
    public ResponseEntity<ApiResponse> paymentSuccessHandler(
            @PathVariable String paymentId,
            @RequestParam String paymentLinkId,
            @RequestHeader("Authorization") String jwt) throws Exception {

        Customer customer = customerService.findCustomerByJwtToken(jwt);

        PaymentLinkResponse paymentResponse;

        PaymentOrder paymentOrder= paymentService
                .getPaymentOrderByPaymentId(paymentLinkId);

        boolean paymentSuccess = paymentService.ProceedPaymentOrder(
                paymentOrder,
                paymentId,
                paymentLinkId
        );
        if(paymentSuccess){
            for(Order order:paymentOrder.getOrders()){
                transactionService.createTransaction(order);
                Seller seller=sellerService.getSellerById(order.getSellerId());
                SellerReport report=sellerReportService.getSellerReport(seller);
                report.setTotalOrders(report.getTotalOrders()+1);
                report.setTotalEarnings(report.getTotalEarnings()+order.getTotalSellingPrice());
                report.setTotalSales(report.getTotalSales()+order.getOrderItems().size());
                sellerReportService.updateSellerReport(report);
            }
            Cart cart= cartRepository.findByCustomerId(customer.getId());
            cart.setCouponPrice(0);
            cart.setCouponCode(null);
            cartRepository.save(cart);

        }

        ApiResponse res = new ApiResponse();
        res.setMessage("Payment successful");
        res.setStatus(true);

        return new ResponseEntity<>(res, HttpStatus.CREATED);
    }
}



