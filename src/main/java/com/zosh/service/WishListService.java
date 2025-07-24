package com.zosh.service;

import com.zosh.model.Customer;
import com.zosh.model.Product;
import com.zosh.model.Wishlist;

public interface WishListService {

    Wishlist createWishList (Customer customer);

    Wishlist getWishListByCustomerId(Customer customer);

    Wishlist addProductToWishList(Customer customer, Product product);
}
