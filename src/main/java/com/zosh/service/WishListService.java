package com.zosh.service;

import com.zosh.exceptions.WishlistNotFoundException;
import com.zosh.model.Customer;
import com.zosh.model.Product;
import com.zosh.model.Wishlist;

public interface WishListService {

    Wishlist createWishList (Customer customer) throws WishlistNotFoundException;

    Wishlist getWishListByCustomerId(Customer customer)throws WishlistNotFoundException;

    Wishlist addProductToWishList(Customer customer, Product product) throws WishlistNotFoundException;
}
