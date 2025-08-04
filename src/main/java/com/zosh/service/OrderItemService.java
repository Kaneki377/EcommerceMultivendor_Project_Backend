package com.zosh.service;

import com.zosh.model.OrderItem;

public interface OrderItemService {
    OrderItem getOrderItemById(long id) throws Exception;
}
