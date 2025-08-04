package com.zosh.service;


import com.zosh.domain.OrderStatus;
import com.zosh.model.*;

import java.util.List;
import java.util.Set;

public interface OrderService {

    Set<Order> createOrder(Customer customer, Address shippingAddress, Cart cart);

    Order findOrderById(long orderId) throws Exception;

    List<Order> customerOrderHistory(Long customerId);

    List<Order> sellersOrder(Long sellerId);

    Order updateOrderStatus(long orderId, OrderStatus orderStatus) throws Exception;

    Order cancelOrder(long orderId, Customer customer) throws Exception;

    OrderItem getOrderItemById(long id) throws Exception;

    public void deleteOrder(Long orderId) throws OrderException;
}
