package com.zosh.service.impl;

import com.zosh.model.Cart;
import com.zosh.model.CartItem;
import com.zosh.model.Customer;
import com.zosh.model.Product;
import com.zosh.repository.CartItemRepository;
import com.zosh.repository.CartRepository;
import com.zosh.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;

    private final CartItemRepository cartItemRepository;

    @Override
    public CartItem addCartItem(Customer customer, Product product, String size, int quantity) {

        Cart cart = findCustomerCart(customer);

        CartItem isPresent = cartItemRepository.findByCartAndProductAndSize(cart, product, size);

        if(isPresent == null) {
            CartItem cartItem = new CartItem();
            cartItem.setProduct(product);
            cartItem.setSize(size);
            cartItem.setQuantity(quantity);
            cartItem.setUserId(customer.getId());

            int totalPrice = quantity * product.getSellingPrice();
            cartItem.setSellingPrice(totalPrice);

            cart.getCartItems().add(cartItem);
            cartItem.setCart(cart);

            return cartItemRepository.save(cartItem);
        }

        return isPresent;
    }

    @Override
    public Cart findCustomerCart(Customer customer) {

        Cart cart = cartRepository.findByCustomerId(customer.getId());

        int totalPrice = 0;
        int totalDiscountedPrice = 0;
        int totalItems = 0;

        for(CartItem cartItem: cart.getCartItems()){
            totalPrice += cartItem.getMrpPrice();
            totalDiscountedPrice += cartItem.getSellingPrice();
            totalItems += cartItem.getQuantity();
        }

        cart.setTotalMrpPrice(totalPrice);
        cart.setTotalItem(totalItems);
        cart.setTotalSellingPrice(totalDiscountedPrice);
        cart.setDiscount(calculateDiscountPercentage(totalPrice, totalDiscountedPrice));
        cart.setTotalItem(totalItems);

        return null;
    }

    private int calculateDiscountPercentage(int mrpPrice, int sellingPrice) {
        if(mrpPrice <= 0) {
            throw new IllegalArgumentException("Actual price must be greater than zero");
        }
        double discount = mrpPrice - sellingPrice;
        double discountPercentage = (discount / mrpPrice) * 100;
        return (int) discountPercentage;
    }


}
