package com.zosh.service;


import com.zosh.domain.OrderStatus;
import com.zosh.exceptions.OrderException;
import com.zosh.model.*;

import java.util.List;
import java.util.Set;

public interface OrderService {

    Set<Order> createOrder(Customer customer, Address shippingAddress, Cart cart);

    Order findOrderById(long orderId) throws OrderException;

    List<Order> customerOrderHistory(Long customerId);

    //Get ShopOrder
    List<Order> sellersOrder(Long sellerId);

    Order updateOrderStatus(long orderId, OrderStatus orderStatus) throws OrderException;

    Order cancelOrder(long orderId, Customer customer) throws Exception;

    public void deleteOrder(Long orderId) throws OrderException;
}
