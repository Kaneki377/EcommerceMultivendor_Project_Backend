package com.zosh.service.impl;

import com.zosh.exceptions.OrderException;
import com.zosh.model.OrderItem;
import com.zosh.repository.OrderItemRepository;
import com.zosh.service.OrderItemService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@AllArgsConstructor
public class OrderItemServiceImpl implements OrderItemService {

    public final OrderItemRepository orderItemRepository;
    @Override
    public OrderItem getOrderItemById(long id) throws Exception {
        System.out.println("------- "+id);
        Optional<OrderItem> orderItem = orderItemRepository.findById(id);
        if(orderItem.isPresent()){
            return orderItem.get();
        }
        throw new OrderException("Order item not found");
    }
}
