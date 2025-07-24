package com.zosh.controller;

import com.razorpay.PaymentLink;
import com.zosh.domain.PaymentMethod;
import com.zosh.model.*;
import com.zosh.repository.PaymentOrderRepository;
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

        if(paymentMethod.equals(PaymentMethod.RAZORPAY)){
            PaymentLink payment=paymentService.createRazorpayPaymentLink(customer,
                    paymentOrder.getAmount(),
                    paymentOrder.getId());
            String paymentUrl=payment.get("short_url");
            String paymentUrlId=payment.get("id");

            res.setPayment_link_url(paymentUrl);
            paymentOrder.setPaymentLinkId(paymentUrlId);
            paymentOrderRepository.save(paymentOrder);
        }
        //Bo sung dong equals PaymentMethod Stripe
        else if(paymentMethod.equals(PaymentMethod.STRIPE)){
            String paymentUrl=paymentService.createStripePaymentLink(customer,
                    paymentOrder.getAmount(),
                    paymentOrder.getId());
            res.setPayment_link_url(paymentUrl);
        }
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