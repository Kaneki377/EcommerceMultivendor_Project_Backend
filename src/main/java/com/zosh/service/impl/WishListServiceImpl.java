package com.zosh.service.impl;

import com.zosh.exceptions.WishlistNotFoundException;
import com.zosh.model.Customer;
import com.zosh.model.Product;
import com.zosh.model.Wishlist;
import com.zosh.repository.WishListRepository;
import com.zosh.service.WishListService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WishListServiceImpl implements WishListService {

    private final WishListRepository wishListRepository;

    @Override
    public Wishlist createWishList(Customer customer) throws WishlistNotFoundException {
        try {
            if (customer == null) {
                throw new WishlistNotFoundException("Customer is null. Cannot create wishlist.");
            }

            Wishlist wishlist = new Wishlist();
            wishlist.setCustomer(customer);

            return wishListRepository.save(wishlist);

        } catch (Exception e) {
            throw new WishlistNotFoundException("Failed to create wishlist: " + e.getMessage());
        }
    }

    @Override
    public Wishlist getWishListByCustomerId(Customer customer) throws WishlistNotFoundException{

        Wishlist wishlist = wishListRepository.findByCustomerId(customer.getId());

        if(wishlist == null) {
            wishlist = createWishList(customer);
        }

        return wishlist;
    }

    @Override
    public Wishlist addProductToWishList(Customer customer, Product product) throws WishlistNotFoundException{
        Wishlist wishlist = getWishListByCustomerId(customer);

        if(wishlist.getProducts().contains(product)) {
            wishlist.getProducts().remove(product);
        }else{
            wishlist.getProducts().add(product);
        }

        return wishListRepository.save(wishlist);
    }
}
