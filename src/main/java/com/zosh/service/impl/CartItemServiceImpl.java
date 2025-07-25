package com.zosh.service.impl;

import com.zosh.model.CartItem;
import com.zosh.model.Customer;
import com.zosh.repository.CartItemRepository;
import com.zosh.service.CartItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CartItemServiceImpl implements CartItemService {

    private final CartItemRepository cartItemRepository;



    @Override
    public CartItem updateCartItem(Long customerId, Long id, CartItem cartItem) throws Exception {
        CartItem item = findCartItemById(id);

        Customer cartItemCustomer = item.getCart().getCustomer();

        if(cartItemCustomer.getId().equals(customerId)) {
            item.setQuantity(cartItem.getQuantity());
            item.setMrpPrice(item.getQuantity() *  item.getProduct().getMrpPrice());
            item.setSellingPrice(item.getQuantity() *  item.getProduct().getSellingPrice());
            return cartItemRepository.save(item);
        }
        throw new Exception("You can't update this cart item");
    }

    @Override
    public void removeCartItem(Long customerId, Long id) throws Exception {
        CartItem item  = findCartItemById(id);

        Customer cartItemCustomer = item.getCart().getCustomer();

        if(cartItemCustomer.getId().equals(customerId)) {
            cartItemRepository.delete(item);
        }else throw new Exception("You can't delete this cart item");
    }

    @Override
    public CartItem findCartItemById(Long id) throws Exception {

        return cartItemRepository.findById(id).orElseThrow(()->
                new Exception("Cart item not found with id " + id));
    }
}
