package com.zosh.service.impl;

import com.zosh.domain.AddressOwnerType;
import com.zosh.domain.OrderStatus;
import com.zosh.domain.PaymentStatus;
import com.zosh.model.*;
import com.zosh.repository.AddressRepository;
import com.zosh.repository.OrderItemRepository;
import com.zosh.repository.OrderRepository;
import com.zosh.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;

    private final AddressRepository addressRepository;

    private final OrderItemRepository orderItemRepository;

    @Override
    public Set<Order> createOrder(Customer customer, Address shippingAddress, Cart cart) {
        if(!customer.getAddresses().contains(shippingAddress)) {
            customer.getAddresses().add(shippingAddress);
            shippingAddress.setOwnerId(customer.getId());
            shippingAddress.setOwnerType(AddressOwnerType.SHIPPING);
        }
        Address address = addressRepository.save(shippingAddress);

        //Trong case: 1 user buy product from different seller

        Map<Long, List<CartItem>> itemsBySeller = cart.getCartItems().stream()
                .collect(Collectors.groupingBy(item -> item.getProduct().
                        getSeller().getId()));

        Set<Order> orders = new HashSet<>();

        for(Map.Entry<Long, List<CartItem>> entry : itemsBySeller.entrySet()) {
            Long sellerId = entry.getKey();
            List<CartItem> items = entry.getValue();

            int totalOrderPrice = items.stream().mapToInt(
                    CartItem::getSellingPrice
            ).sum();

            int totalItem = items.stream().mapToInt(
                    CartItem::getQuantity
            ).sum();

            Order createdOrder = new Order();
            createdOrder.setCustomer(customer);
            createdOrder.setSellerId(sellerId);
            createdOrder.setTotalMrpPrice(totalOrderPrice);
            createdOrder.setTotalSellingPrice(totalOrderPrice);
            createdOrder.setTotalItem(totalItem);
            createdOrder.setShippingAddress(address);
            createdOrder.setOrderStatus(OrderStatus.PENDING);
            createdOrder.getPaymentDetails().setStatus(PaymentStatus.PENDING);

            Order savedOrder = orderRepository.save(createdOrder);
            orders.add(savedOrder);

            List<OrderItem> orderItems = new ArrayList<>();

            for(CartItem item: items){
                OrderItem orderItem = new OrderItem();
                orderItem.setOrder(savedOrder);
                orderItem.setMrpPrice(item.getMrpPrice());
                orderItem.setProduct(item.getProduct());
                orderItem.setQuantity(item.getQuantity());
                orderItem.setSize(item.getSize());
                orderItem.setSellingPrice(item.getSellingPrice());
                orderItem.setCustomerId(item.getCustomerId());

                savedOrder.getOrderItems().add(orderItem);

                OrderItem savedOrderItem = orderItemRepository.save(orderItem);
                orderItems.add(savedOrderItem);
            }
        }

        return orders;
    }

    @Override
    public Order findOrderById(long orderId) throws Exception {
        return orderRepository.findById(orderId).orElseThrow(()->
                new Exception("Order not found..."));
    }

    @Override
    public List<Order> customerOrderHistory(Long customerId) {
        return orderRepository.findByCustomerId(customerId);
    }

    @Override
    public List<Order> sellersOrder(Long sellerId) {
        return orderRepository.findBySellerIdOrderByOrderDateDesc(sellerId);
    }

    @Override
    public Order updateOrderStatus(long orderId, OrderStatus orderStatus) throws Exception {
        Order order = findOrderById(orderId);
        order.setOrderStatus(orderStatus);

        return orderRepository.save(order);
    }

    @Override
    public Order cancelOrder(long orderId, Customer customer) throws Exception {
        Order order = findOrderById(orderId);

        if(!customer.getId().equals(order.getCustomer().getId())) {
            throw new Exception("You don't have access to this order");
        }
        order.setOrderStatus(OrderStatus.CANCELLED);

        return orderRepository.save(order);
    }

    @Override
    public OrderItem getOrderItemById(long id) throws Exception {
        return orderItemRepository.findById(id).orElseThrow(()->
                new Exception("Order item not exist..."));
    }
}
