package com.zosh.service;


import com.zosh.exceptions.CartItemException;
import com.zosh.exceptions.CustomerException;
import com.zosh.model.CartItem;


public interface CartItemService {

    CartItem updateCartItem(Long customerId, Long id, CartItem cartItem) throws CartItemException, CustomerException;

    void removeCartItem(Long customerId, Long id) throws CartItemException, CustomerException;

    CartItem findCartItemById(Long id) throws CartItemException;
}
