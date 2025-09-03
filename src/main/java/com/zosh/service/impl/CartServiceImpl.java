package com.zosh.service.impl;

import com.zosh.model.*;
import com.zosh.repository.AffiliateLinkRepository;
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
    private final AffiliateLinkRepository affiliateLinkRepository;

    @Override
    public CartItem addCartItem(Customer customer, Product product, String size, int quantity) {

        Cart cart = findCustomerCart(customer);

        CartItem isPresent = cartItemRepository.findByCartAndProductAndSize(cart, product, size);

        if (isPresent == null) {
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
        }

        return isPresent;
    }

    @Override
    public CartItem addCartItem(Customer customer, Product product, String size, int quantity,
            String kocCode, String campaignCode) {
        Cart cart = findCustomerCart(customer);
        CartItem isPresent = cartItemRepository.findByCartAndProductAndSize(cart, product, size);

        if (isPresent == null) {
            CartItem cartItem = new CartItem();
            cartItem.setProduct(product);
            cartItem.setSize(size);
            cartItem.setQuantity(quantity);
            cartItem.setCustomerId(customer.getId());

            int totalPrice = quantity * product.getSellingPrice();
            cartItem.setSellingPrice(totalPrice);
            cartItem.setMrpPrice(quantity * product.getMrpPrice());

            // 🎯 Affiliate tracking - tìm AffiliateLink dựa trên kocCode và campaignCode
            if (kocCode != null && campaignCode != null) {
                AffiliateLink affiliateLink = affiliateLinkRepository
                        .findByKoc_KocCodeAndCampaign_CampaignCodeAndProduct_Id(kocCode, campaignCode, product.getId())
                        .orElse(null);

                if (affiliateLink != null) {
                    cartItem.setAffiliateLink(affiliateLink);
                    System.out.println("✅ Found and set affiliate link: " + affiliateLink.getId());
                } else {
                    System.out.println("❌ No affiliate link found for KOC: " + kocCode + ", Campaign: " + campaignCode
                            + ", Product: " + product.getId());
                }
            }

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

        for (CartItem cartItem : cart.getCartItems()) {
            totalPrice += cartItem.getMrpPrice();
            totalDiscountedPrice += cartItem.getSellingPrice();
            totalItems += cartItem.getQuantity();
        }

        cart.setTotalMrpPrice(totalPrice);
        cart.setCouponCode(cart.getCouponCode());
        cart.setCouponPrice(cart.getCouponPrice());
        cart.setTotalItem(cart.getCartItems().size());
        cart.setTotalSellingPrice(totalDiscountedPrice - cart.getCouponPrice());
        cart.setDiscount(calculateDiscountPercentage(totalPrice, totalDiscountedPrice));
        cart.setTotalItem(totalItems);

        return cartRepository.save(cart);
    }

    private int calculateDiscountPercentage(int mrpPrice, int sellingPrice) {
        if (mrpPrice <= 0) {
            return 0;
        }
        double discount = mrpPrice - sellingPrice;
        double discountPercentage = (discount / mrpPrice) * 100;
        return (int) discountPercentage;
    }

}
