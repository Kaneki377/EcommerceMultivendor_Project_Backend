package com.zosh.service.impl;

import com.zosh.model.*;
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
    public CartItem addCartItem(Customer customer, Product product, String size, int quantity, AffiliateLink affiliateLink) {

        Cart cart = findCustomerCart(customer);

        CartItem isPresent = (affiliateLink == null)
                ? cartItemRepository.findByCartAndProductAndSize(cart, product, size)
                : cartItemRepository.findByCartAndProductAndSizeAndAffiliateLink(cart, product, size, affiliateLink);

        if(isPresent == null) {
            CartItem cartItem = new CartItem();
            cartItem.setProduct(product);
            cartItem.setSize(size);
            cartItem.setQuantity(quantity);
            cartItem.setCustomerId(customer.getId());
            cartItem.setAffiliateLink(affiliateLink);

            int totalPrice = quantity * product.getSellingPrice();
            cartItem.setSellingPrice(totalPrice);
            cartItem.setMrpPrice(quantity * product.getMrpPrice());

            cart.getCartItems().add(cartItem);
            cartItem.setCart(cart);

            CartItem saved = cartItemRepository.save(cartItem);

            // (tuỳ) cập nhật lại totals của cart nếu bạn muốn tính tức thời
            findCustomerCart(customer);

            return saved;
        }else{
            int newQty = isPresent.getQuantity() + quantity;
            isPresent.setQuantity(newQty);
            isPresent.setSellingPrice(newQty * product.getSellingPrice());
            isPresent.setMrpPrice(newQty * product.getMrpPrice());
            CartItem saved = cartItemRepository.save(isPresent);

            // (tuỳ) cập nhật lại totals
            findCustomerCart(customer);

            return saved;
        }
    }

    @Override
    public CartItem addCartItem(Customer customer, Product product, String size, int quantity) {

        Cart cart = findCustomerCart(customer);

        CartItem isPresent = cartItemRepository.findByCartAndProductAndSize(cart, product, size);

        if(isPresent == null) {
            CartItem cartItem = new CartItem();
            cartItem.setProduct(product);
            cartItem.setSize(size);
            cartItem.setQuantity(quantity);
            cartItem.setCustomerId(customer.getId());

            int totalPrice = quantity * product.getSellingPrice();
            cartItem.setSellingPrice(totalPrice);
            cartItem.setMrpPrice(quantity * product.getMrpPrice());

            cart.getCartItems().add(cartItem);
            cartItem.setCart(cart);


            return cartItemRepository.save(cartItem);
        }else{
            int newQty = isPresent.getQuantity() + quantity;
            isPresent.setQuantity(newQty);
            isPresent.setSellingPrice(newQty * product.getSellingPrice());
            isPresent.setMrpPrice(newQty * product.getMrpPrice());
            return cartItemRepository.save(isPresent);
        }
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
        cart.setCouponCode(cart.getCouponCode());
        cart.setCouponPrice(cart.getCouponPrice());
        cart.setTotalItem(cart.getCartItems().size());
        cart.setTotalSellingPrice(totalDiscountedPrice-cart.getCouponPrice());
        cart.setDiscount(calculateDiscountPercentage(totalPrice, totalDiscountedPrice));
        cart.setTotalItem(totalItems);

        return cartRepository.save(cart);
    }

    private int calculateDiscountPercentage(int mrpPrice, int sellingPrice) {
        if(mrpPrice <= 0) {
            return 0;
        }
        double discount = mrpPrice - sellingPrice;
        double discountPercentage = (discount / mrpPrice) * 100;
        return (int) discountPercentage;
    }


}
