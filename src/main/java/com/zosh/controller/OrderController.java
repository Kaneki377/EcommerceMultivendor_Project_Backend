package com.zosh.controller;

import com.paypal.base.exception.PayPalException;
import com.paypal.base.rest.PayPalRESTException;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.zosh.domain.PaymentMethod;
import com.zosh.domain.PaymentOrderStatus;
import com.zosh.domain.PaymentStatus;
import com.zosh.exceptions.CustomerException;
import com.zosh.exceptions.OrderException;
import com.zosh.exceptions.SellerException;
import com.zosh.model.*;
import com.zosh.repository.OrderRepository;
import com.zosh.repository.PaymentOrderRepository;
import com.zosh.repository.ProductRepository;
import com.zosh.request.PaypalCompletionRequest;
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
    private final OrderItemService orderItemService;
    private final ProductRepository productRepository;
    //Tạo đơn hàng mới và tạo link thanh toán Stripe , Paypal
    @PostMapping()
    public ResponseEntity<PaymentLinkResponse> createOrderHandler(
            @RequestBody Address shippingAddress,
            @RequestParam PaymentMethod paymentMethod,
            @RequestHeader("Authorization")String jwt)
            throws CustomerException, PayPalRESTException, StripeException {

        Customer customer = customerService.findCustomerProfileByJwt(jwt);
        Cart cart= cartService.findCustomerCart(customer);
        Set<Order> orders =orderService.createOrder(customer, shippingAddress, cart);

        PaymentOrder paymentOrder = paymentService.createOrder(customer,orders);

        PaymentLinkResponse res = new PaymentLinkResponse();
        String paymentUrl = "";

        if (paymentMethod.equals(PaymentMethod.STRIPE)) {
            // Stripe
            PaymentLinkResponse stripeRes = paymentService.createStripePaymentLink(customer,
                    paymentOrder.getAmount(),
                    paymentOrder.getId());

            res.setPayment_link_url(stripeRes.getPayment_link_url());
            res.setPayment_link_id(stripeRes.getPayment_link_id());

            paymentOrder.setPaymentLinkId(stripeRes.getPayment_link_id());
            paymentOrderRepository.save(paymentOrder);
        }
        // Thêm khối else if cho PayPal
        else if (paymentMethod.equals(PaymentMethod.PAYPAL)) {
            paymentUrl = paymentService.createPaypalPaymentLink(
                    paymentOrder.getAmount(),
                    paymentOrder.getId()
            );
            res.setPayment_link_url(paymentUrl);
        }
        else if (paymentMethod.equals(PaymentMethod.COD)) {
            // COD
            for (Order order : orders) {
                order.getPaymentDetails().setPaymentMethod(PaymentMethod.COD);
                order.setPaymentStatus(PaymentStatus.PENDING); // Chưa trả tiền
                // ✅ trừ stock ngay khi tạo order
                for (OrderItem item : order.getOrderItems()) {
                    Product product = item.getProduct();
                    product.setQuantity(product.getQuantity() - item.getQuantity());
                    productRepository.save(product);
                }
                orderRepository.save(order);
            }
            // Với COD thì không cần payment link, chỉ trả về message
            res.setPayment_link_url(null);
            res.setPayment_link_id(null);
        }
        return new ResponseEntity<>(res, HttpStatus.OK);
    }

    @GetMapping("/customer")
    public ResponseEntity<List<Order>> customersOrderHistoryHandler(
            @RequestHeader("Authorization")
            String jwt) throws CustomerException{

        Customer customer= customerService.findCustomerProfileByJwt(jwt);
        List<Order> orders=orderService.customerOrderHistory(customer.getId());
        return new ResponseEntity<>(orders,HttpStatus.ACCEPTED);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity< Order> getOrderById(@PathVariable Long orderId, @RequestHeader("Authorization")
    String jwt) throws OrderException, CustomerException{

        Customer customer = customerService.findCustomerProfileByJwt(jwt);
        Order orders=orderService.findOrderById(orderId);
        return new ResponseEntity<>(orders,HttpStatus.ACCEPTED);
    }

    @GetMapping("/item/{orderItemId}")
    public ResponseEntity<OrderItem> getOrderItemById(
            @PathVariable Long orderItemId, @RequestHeader("Authorization")
            String jwt) throws Exception {
        System.out.println("------- controller ");
        Customer customer = customerService.findCustomerProfileByJwt(jwt);
        OrderItem orderItem = orderItemService.getOrderItemById(orderItemId);
        return new ResponseEntity<>(orderItem,HttpStatus.ACCEPTED);
    }

    @PutMapping("/{orderId}/cancel")
    public ResponseEntity<Order> cancelOrder(
            @PathVariable Long orderId,
            @RequestHeader("Authorization") String jwt
    ) throws CustomerException, OrderException, SellerException {
        Customer customer= customerService.findCustomerProfileByJwt(jwt);
        Order order=orderService.cancelOrder(orderId, customer);

        Seller seller = sellerService.getSellerById(order.getSellerId());
        SellerReport report = sellerReportService.getSellerReport(seller);

        report.setCanceledOrders(report.getCanceledOrders()+1);
        if (order.getPaymentStatus() == PaymentStatus.COMPLETED) {
            report.setTotalRefunds(report.getTotalRefunds() + order.getTotalSellingPrice());
        }
        sellerReportService.updateSellerReport(report);

        return ResponseEntity.ok(order);
    }

    //Paypal
    @PostMapping("/paypal/complete")
    public ResponseEntity<ApiResponse> completePaypalPayment(
            @RequestBody PaypalCompletionRequest request, // Tạo một class DTO mới
            @RequestHeader("Authorization") String jwt) throws Exception {

        // Xác thực người dùng qua JWT
        Customer customer = customerService.findCustomerProfileByJwt(jwt);

        // Gọi service để thực thi thanh toán và cập nhật đơn hàng
        paymentService.executeAndCompletePaypalOrder(
                customer,
                request.getPaymentId(),
                request.getPayerId(),
                request.getPaymentOrderId()
        );

        ApiResponse res = new ApiResponse();
        res.setMessage("Payment Completed Successfully");
        return new ResponseEntity<>(res, HttpStatus.OK);
    }

}