package com.zosh.service.impl;

import com.zosh.exceptions.CartItemException;
import com.zosh.exceptions.CustomerException;
import com.zosh.model.CartItem;
import com.zosh.model.Customer;
import com.zosh.repository.CartItemRepository;
import com.zosh.service.CartItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CartItemServiceImpl implements CartItemService {

    private final CartItemRepository cartItemRepository;



    @Override
    public CartItem updateCartItem(Long customerId, Long id, CartItem cartItem) throws CartItemException {
        CartItem item = findCartItemById(id);

        Customer cartItemCustomer = item.getCart().getCustomer();

        if(cartItemCustomer.getId().equals(customerId)) {
            item.setQuantity(cartItem.getQuantity());
            item.setMrpPrice(item.getQuantity() *  item.getProduct().getMrpPrice());
            item.setSellingPrice(item.getQuantity() *  item.getProduct().getSellingPrice());
            return cartItemRepository.save(item);
        }else {
            throw new CartItemException("You can't update this cart item");
        }
    }

    @Override
    public void removeCartItem(Long customerId, Long cartItemId) throws CartItemException, CustomerException {
        System.out.println("customerId- "+customerId+" cartItemId "+ cartItemId);

        CartItem cartItem  = findCartItemById(cartItemId);

        Customer cartItemCustomer = cartItem.getCart().getCustomer();

        if(cartItemCustomer.getId().equals(customerId)) {
            cartItemRepository.deleteById(cartItem.getId());
        }else throw new CustomerException("You can't delete this cart item");
    }

    @Override
    public CartItem findCartItemById(Long cartItemId) throws CartItemException {

        Optional<CartItem> opt=cartItemRepository.findById(cartItemId);

        if(opt.isPresent()) {
            return opt.get();
        }
        throw new CartItemException("cartItem not found with id : "+cartItemId);
    }
}
