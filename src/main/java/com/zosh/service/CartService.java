package com.zosh.service;

import com.zosh.model.*;

public interface CartService {

    public CartItem addCartItem(Customer customer, Product product, String size, int quantity, AffiliateLink affiliateLink);

    public CartItem addCartItem(
            Customer customer,
            Product product,
            String size,
            int quantity);

    public Cart findCustomerCart(Customer customer);
}
