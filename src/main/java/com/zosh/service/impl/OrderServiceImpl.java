package com.zosh.service.impl;

import com.zosh.domain.OrderStatus;
import com.zosh.model.*;
import com.zosh.repository.AddressReposity;
import com.zosh.repository.OrderRepository;
import com.zosh.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;

    private final AddressReposity addressRepository;

    @Override
    public Set<Order> createOrder(Customer customer, Address shippingAddress, Cart cart) {
        if(!customer.getAddresses().contains(shippingAddress)) {
            customer.getAddresses().add(shippingAddress);
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
            createdOrder.setShippingAddress(shippingAddress);

        }

        return Set.of();
    }

    @Override
    public Order findOrderById(long orderId) {
        return null;
    }

    @Override
    public List<Order> customerOrderHistory(Long customerId) {
        return List.of();
    }

    @Override
    public List<Order> sellersOrder(Long sellerId) {
        return List.of();
    }

    @Override
    public Order updateOrderStatus(long orderId, OrderStatus orderStatus) {
        return null;
    }

    @Override
    public Order cancelOrder(long orderId, Customer customer) {
        return null;
    }
}
