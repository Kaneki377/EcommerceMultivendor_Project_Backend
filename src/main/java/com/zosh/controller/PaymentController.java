package com.zosh.controller;

import com.zosh.domain.PaymentMethod;
import com.zosh.model.*;
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

    @GetMapping("/{paymentId}")
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
                Seller seller = sellerService.getSellerById(order.getSellerId());
                SellerReport report=sellerReportService.getSellerReport(seller);
                report.setTotalOrders(report.getTotalOrders()+1);
                report.setTotalEarnings(report.getTotalEarnings()+order.getTotalSellingPrice());
                report.setTotalSales(report.getTotalSales()+order.getOrderItems().size());
                sellerReportService.updateSellerReport(report);
            }
        }

        ApiResponse res = new ApiResponse();
        res.setMessage("Payment successful");

        return new ResponseEntity<>(res, HttpStatus.CREATED);
    }

    @GetMapping("/paypal/success")
    public RedirectView paypalPaymentSuccess(
            @RequestParam("paymentId") String paymentId,
            @RequestParam("PayerID") String payerId,
            @RequestParam("paymentOrderId") Long paymentOrderId) throws Exception {

        PaymentOrder paymentOrder = paymentService.getPaymentOrderById(paymentOrderId);

        try {
            com.paypal.api.payments.Payment payment = paymentService.executePaypalPayment(paymentId, payerId);

            if ("approved".equalsIgnoreCase(payment.getState())) {
                // Cập nhật trạng thái PaymentOrder
                paymentOrder.setPaymentLinkId(paymentId); // Lưu paymentId của PayPal
                paymentOrder.setStatus(com.zosh.domain.PaymentOrderStatus.SUCCESS);
                paymentOrderRepository.save(paymentOrder);

                // Cập nhật trạng thái các Order con và tạo Transaction
                for (Order order : paymentOrder.getOrders()) {
                    order.setPaymentStatus(com.zosh.domain.PaymentStatus.COMPLETED);
                    orderRepository.save(order); // Lưu lại order

                    transactionService.createTransaction(order);
                    Seller seller = sellerService.getSellerById(order.getSellerId());
                    SellerReport report = sellerReportService.getSellerReport(seller);
                    report.setTotalOrders(report.getTotalOrders() + 1);
                    report.setTotalEarnings(report.getTotalEarnings() + order.getTotalSellingPrice());
                    report.setTotalSales(report.getTotalSales() + order.getOrderItems().size());
                    sellerReportService.updateSellerReport(report);
                }
            }
        } catch (Exception e) {
            // Xử lý lỗi nếu execute payment thất bại
            paymentOrder.setStatus(com.zosh.domain.PaymentOrderStatus.FAILED);
            paymentOrderRepository.save(paymentOrder);
            // Chuyển hướng về trang lỗi của frontend
            return new RedirectView("http://localhost:3000/payment/failed");
        }

        // Chuyển hướng người dùng về trang thành công của frontend
        return new RedirectView("http://localhost:3000/payment-success/" + paymentOrderId);
    }

    // Endpoint xử lý callback hủy bỏ từ PayPal
    @GetMapping("/paypal/cancel")
    public RedirectView paypalPaymentCancel(@RequestParam("paymentOrderId") Long paymentOrderId) throws Exception {
        PaymentOrder paymentOrder = paymentService.getPaymentOrderById(paymentOrderId);
        paymentOrder.setStatus(com.zosh.domain.PaymentOrderStatus.FAILED);
        paymentOrderRepository.save(paymentOrder);

        // Chuyển hướng về trang giỏ hàng hoặc trang thông báo hủy của frontend
        return new RedirectView("http://localhost:3000/payment/cancel");
    }
}
