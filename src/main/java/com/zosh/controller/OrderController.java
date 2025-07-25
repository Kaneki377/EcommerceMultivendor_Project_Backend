package com.zosh.controller;

import com.zosh.domain.PaymentMethod;
import com.zosh.domain.PaymentOrderStatus;
import com.zosh.domain.PaymentStatus;
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
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    private final CustomerService customerService;

    private final CartService cartService;

    private final SellerService sellerService;

    private final SellerReportService sellerReportService;

    private final PaymentService paymentService;

    private final PaymentOrderRepository paymentOrderRepository;

    private final OrderRepository orderRepository;

    private final TransactionService transactionService;

    @PostMapping()
    public ResponseEntity<PaymentLinkResponse> createOrderHandler(
            @RequestBody Address spippingAddress,
            @RequestParam PaymentMethod paymentMethod,
            @RequestHeader("Authorization")String jwt)
            throws Exception{

        Customer customer = customerService.findCustomerByJwtToken(jwt);
        Cart cart= cartService.findCustomerCart(customer);
        Set<Order> orders =orderService.createOrder(customer, spippingAddress, cart);

        PaymentOrder paymentOrder = paymentService.createOrder(customer,orders);

        PaymentLinkResponse res = new PaymentLinkResponse();
        String paymentUrl = "";

        if(paymentMethod.equals(PaymentMethod.STRIPE)){
            paymentUrl = paymentService.createStripePaymentLink(customer,
                    paymentOrder.getAmount(),
                    paymentOrder.getId());
        }
        // Thêm khối else if cho PayPal
        else if (paymentMethod.equals(PaymentMethod.PAYPAL)) {
            paymentUrl = paymentService.createPaypalPaymentLink(
                    paymentOrder.getAmount(),
                    paymentOrder.getId()
            );
        }

        res.setPayment_link_url(paymentUrl);

        return new ResponseEntity<>(res, HttpStatus.OK);
    }

    @PutMapping("/payment-order/{paymentOrderId}/complete")
    public ResponseEntity<ApiResponse> completePayment(
            @PathVariable Long paymentOrderId, // Nhận paymentOrderId
            @RequestHeader("Authorization") String jwt) throws Exception {

        // 1. Xác thực người dùng
        Customer customer = customerService.findCustomerByJwtToken(jwt);

        // 2. Tìm PaymentOrder
        PaymentOrder paymentOrder = paymentService.getPaymentOrderById(paymentOrderId);

        // Kiểm tra xem người dùng có quyền truy cập không
        if (!paymentOrder.getCustomer().getId().equals(customer.getId())) {
            throw new Exception("You do not have permission to update this payment order.");
        }

        // 3. Cập nhật trạng thái của PaymentOrder
        paymentOrder.setStatus(PaymentOrderStatus.SUCCESS);
        paymentOrderRepository.save(paymentOrder);

        // 4. Lặp qua TẤT CẢ các Order con và cập nhật chúng
        for (Order order : paymentOrder.getOrders()) {
            order.setPaymentStatus(PaymentStatus.COMPLETED);
            orderRepository.save(order);

            // Tạo transaction và cập nhật report cho từng order
            transactionService.createTransaction(order);
            Seller seller = sellerService.getSellerById(order.getSellerId());
            SellerReport report = sellerReportService.getSellerReport(seller);
            report.setTotalOrders(report.getTotalOrders() + 1);
            report.setTotalEarnings(report.getTotalEarnings() + order.getTotalSellingPrice());
            report.setTotalSales(report.getTotalSales() + order.getOrderItems().size());
            sellerReportService.updateSellerReport(report);
        }

        // 5. Trả về kết quả
        ApiResponse res = new ApiResponse();
        res.setMessage("Payment completed and all related orders have been updated.");
        return new ResponseEntity<>(res, HttpStatus.OK);
    }

    @GetMapping("/customer")
    public ResponseEntity<List<Order>> customersOrderHistoryHandler(
            @RequestHeader("Authorization")
            String jwt) throws Exception{

        Customer customer= customerService.findCustomerByJwtToken(jwt);
        List<Order> orders=orderService.customerOrderHistory(customer.getId());
        return new ResponseEntity<>(orders,HttpStatus.ACCEPTED);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity< Order> getOrderById(@PathVariable Long orderId, @RequestHeader("Authorization")
    String jwt) throws Exception{

        Customer customer = customerService.findCustomerByJwtToken(jwt);
        Order orders=orderService.findOrderById(orderId);
        return new ResponseEntity<>(orders,HttpStatus.ACCEPTED);
    }

    @GetMapping("/item/{orderItemId}")
    public ResponseEntity<OrderItem> getOrderItemById(
            @PathVariable Long orderItemId, @RequestHeader("Authorization")
            String jwt) throws Exception {
        System.out.println("------- controller ");
        Customer customer = customerService.findCustomerByJwtToken(jwt);
        OrderItem orderItem = orderService.getOrderItemById(orderItemId);
        return new ResponseEntity<>(orderItem,HttpStatus.ACCEPTED);
    }

    @PutMapping("/{orderId}/cancel")
    public ResponseEntity<Order> cancelOrder(
            @PathVariable Long orderId,
            @RequestHeader("Authorization") String jwt
    ) throws Exception {
        Customer customer= customerService.findCustomerByJwtToken(jwt);
        Order order=orderService.cancelOrder(orderId, customer);

        Seller seller = sellerService.getSellerById(order.getSellerId());
        SellerReport report = sellerReportService.getSellerReport(seller);

        report.setCanceledOrders(report.getCanceledOrders()+1);
        report.setTotalRefunds(report.getTotalRefunds()+order.getTotalSellingPrice());
        sellerReportService.updateSellerReport(report);

        return ResponseEntity.ok(order);
    }

}