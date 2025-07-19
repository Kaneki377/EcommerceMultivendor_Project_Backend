package com.zosh.service;


import com.zosh.model.CartItem;


public interface CartItemService {

    CartItem updateCartItem(Long customerId, Long id, CartItem cartItem) throws Exception;

    void removeCartItem(Long customerId, Long id) throws Exception;

    CartItem findCartItemById(Long id) throws Exception;
}
