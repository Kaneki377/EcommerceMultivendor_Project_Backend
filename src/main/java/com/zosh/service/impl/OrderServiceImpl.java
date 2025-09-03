package com.zosh.service.impl;

import com.zosh.domain.AddressOwnerType;
import com.zosh.domain.OrderStatus;
import com.zosh.domain.PaymentMethod;
import com.zosh.domain.PaymentStatus;
import com.zosh.exceptions.OrderException;
import com.zosh.model.*;
import com.zosh.repository.*;
import com.zosh.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;
import com.zosh.model.AffiliateCommission.CommissionStatus;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final AddressRepository addressRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;
    private final PaymentService paymentService;
    private final AffiliateCommissionService affiliateCommissionService;
    private final CustomerRepository customerRepository;
    @Override
    @Transactional
    public Set<Order> createOrder(Customer customer, Address shippingAddress, Cart cart) {
        // set owner cho chắc chắn
        shippingAddress.setOwnerId(customer.getId());
        shippingAddress.setOwnerType(AddressOwnerType.SHIPPING);

        // lưu address
        Address address = addressRepository.save(shippingAddress);

        // kiểm tra xem customer đã có address này chưa
        boolean alreadyLinked = customer.getAddresses().stream()
                .anyMatch(a -> Objects.equals(a.getId(), address.getId()));

        if (!alreadyLinked) {
            customer.getAddresses().add(address);
            customerRepository.save(customer);
        }

        // Gom các CartItem theo từng Seller

        Map<Long, List<CartItem>> itemsBySeller = cart.getCartItems().stream()
                .collect(Collectors.groupingBy(item -> item.getProduct().getSeller().getId()));

        // Tạo đơn hàng cho từng seller
        Set<Order> orders = new HashSet<>();

        for (Map.Entry<Long, List<CartItem>> entry : itemsBySeller.entrySet()) {
            Long sellerId = entry.getKey();
            List<CartItem> items = entry.getValue();

            int totalOrderPrice = items.stream().mapToInt(
                    CartItem::getSellingPrice).sum();

            int totalItem = items.stream().mapToInt(
                    CartItem::getQuantity).sum();
            // Khởi tạo và lưu Order
            Order createdOrder = new Order();
            createdOrder.setCustomer(customer);
            createdOrder.setSellerId(sellerId);
            createdOrder.setTotalMrpPrice(totalOrderPrice);
            createdOrder.setTotalSellingPrice(totalOrderPrice);
            createdOrder.setTotalItem(totalItem);
            createdOrder.setShippingAddress(address);
            createdOrder.setOrderStatus(OrderStatus.PENDING);

            Order savedOrder = orderRepository.save(createdOrder);
            orders.add(savedOrder);

            // Tạo OrderItem cho từng CartItem
            List<OrderItem> orderItems = new ArrayList<>();

            for (CartItem item : items) {
                OrderItem orderItem = new OrderItem();
                orderItem.setOrder(savedOrder);
                orderItem.setMrpPrice(item.getMrpPrice());
                orderItem.setProduct(item.getProduct());
                orderItem.setQuantity(item.getQuantity());
                orderItem.setSize(item.getSize());
                orderItem.setSellingPrice(item.getSellingPrice());
                orderItem.setCustomerId(item.getCustomerId());

                // 🎯 Copy affiliate link từ CartItem sang OrderItem
                if (item.getAffiliateLink() != null) {
                    orderItem.setAffiliateLink(item.getAffiliateLink());
                    System.out.println("✅ Copied affiliate link to OrderItem: " + item.getAffiliateLink().getId());
                }

                savedOrder.getOrderItems().add(orderItem);

                OrderItem savedOrderItem = orderItemRepository.save(orderItem);
                orderItems.add(savedOrderItem);
            }
            Order hydrated = orderRepository.findById(savedOrder.getId()).orElseThrow();
            affiliateCommissionService.createCommissionsForOrder(hydrated);
        }

        return orders;
    }

    @Override
    public Order findOrderById(long orderId) throws OrderException {
        return orderRepository.findById(orderId).orElseThrow(() -> new OrderException("Order not found..."));
    }

    @Override
    public List<Order> customerOrderHistory(Long customerId) {
        return orderRepository.findByCustomerIdOrderByOrderDateDesc(customerId);
    }

    // Get ShopOrder
    @Override
    @Transactional(readOnly = true)
    public List<Order> sellersOrder(Long sellerId) {
        return orderRepository.findBySellerIdOrderByOrderDateDesc(sellerId);
    }

    @Override
    @Transactional
    public Order updateOrderStatus(long orderId, OrderStatus orderStatus) throws Exception {
        Order order = findOrderById(orderId);

        // Nếu không đổi trạng thái thì thôi cho an toàn
        if (order.getOrderStatus() == orderStatus) {
            return order;
        }

        var now = java.time.LocalDateTime.now();

        // Gắn mốc thời gian tối thiểu cần có
        switch (orderStatus) {
            case CONFIRMED: // = Packed
                if (order.getPackedDate() == null) {
                    order.setPackedDate(now);
                }
                break;

            case DELIVERED:
                // Nếu muốn giữ deliverDate như ETA cũ thì đổi thành: if (order.getDeliverDate()
                // == null) ...
                order.setDeliverDate(now);
                // ✅ Auto-complete COD payment
                try {
                    // Kiểm tra null safety cho PaymentDetails
                    if (order.getPaymentDetails() != null
                            && order.getPaymentDetails().getPaymentMethod() == PaymentMethod.COD
                            && order.getPaymentStatus() == PaymentStatus.PENDING) {
                        System.out.println("🔄 Completing COD payment for order: " + order.getId());
                        paymentService.completePaymentForOrder(order);
                    } else if (order.getPaymentDetails() == null) {
                        System.out.println("⚠️ PaymentDetails is null for order: " + order.getId()
                                + ", skipping payment completion");
                    } else {
                        System.out.println("ℹ️ Order " + order.getId() + " payment method: " +
                                (order.getPaymentDetails() != null ? order.getPaymentDetails().getPaymentMethod()
                                        : "NULL")
                                +
                                ", status: " + order.getPaymentStatus());
                    }
                } catch (Exception e) {
                    System.err.println("❌ Error completing payment for order " + order.getId() + ": " + e.getMessage());
                    e.printStackTrace();
                    throw new RuntimeException("Failed to complete payment for order " + order.getId(), e);
                }
                // ✅ XÁC NHẬN HOA HỒNG KHI GIAO THÀNH CÔNG
                affiliateCommissionService.updateCommissionStatus(orderId, CommissionStatus.CONFIRMED);
                break;
            case CANCELLED:
                // ❌ HỦY HOA HỒNG KHI ĐƠN KHÔNG THÀNH CÔNG
                affiliateCommissionService.updateCommissionStatus(orderId, CommissionStatus.CANCELLED);
                break;

            default:
                // các trạng thái khác không đụng gì
                break;
        }

        order.setOrderStatus(orderStatus);

        return orderRepository.save(order);
    }

    @Override
    public Order cancelOrder(long orderId, Customer customer) throws OrderException {
        Order order = findOrderById(orderId);

        if (!customer.getId().equals(order.getCustomer().getId())) {
            throw new OrderException("You don't have access to this order");
        }
        if (order.getOrderStatus() == OrderStatus.CANCELLED) {
            return order;
        }
        order.setOrderStatus(OrderStatus.CANCELLED);
        // ✅ cộng lại stock cho sản phẩm trong order
        for (OrderItem item : order.getOrderItems()) {
            Product product = item.getProduct();
            product.setQuantity(product.getQuantity() + item.getQuantity());
            productRepository.save(product); // hoặc cascade nếu đã thiết lập
        }

// ❌ hủy commission của đơn này
        affiliateCommissionService.updateCommissionStatus(orderId, CommissionStatus.CANCELLED);
        return orderRepository.save(order);
    }

    @Override
    public void deleteOrder(Long orderId) throws OrderException {
        Order order = findOrderById(orderId);

        orderRepository.deleteById(orderId);
    }

}
