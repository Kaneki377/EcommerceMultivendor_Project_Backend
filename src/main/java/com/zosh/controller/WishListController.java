package com.zosh.controller;

import com.zosh.exceptions.WishlistNotFoundException;
import com.zosh.model.Customer;
import com.zosh.model.Product;
import com.zosh.model.User;
import com.zosh.model.Wishlist;
import com.zosh.service.CustomerService;
import com.zosh.service.ProductService;
import com.zosh.service.WishListService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/wishlist")
public class WishListController {

    private final CustomerService customerService;

    private final WishListService wishListService;

    private final ProductService productService;

    @PostMapping("/create")
    public ResponseEntity<Wishlist> createWishlist(@RequestBody Customer customer) throws WishlistNotFoundException {
        Wishlist wishlist = wishListService.createWishList(customer);
        return ResponseEntity.ok(wishlist);
    }

    @GetMapping()
    public ResponseEntity<Wishlist> getWishlistByCustomerId(
            @RequestHeader("Authorization") String jwt) throws WishlistNotFoundException {

        Customer customer = customerService.findCustomerProfileByJwt(jwt);
        Wishlist wishlist = wishListService.getWishListByCustomerId(customer);
        return ResponseEntity.ok(wishlist);
    }

        @PostMapping("/add-product/{productId}")
    public ResponseEntity<Wishlist> addProductToWishlist(
            @PathVariable Long productId,
            @RequestHeader("Authorization") String jwt) throws WishlistNotFoundException {

        Product product = productService.findProductById(productId);
        Customer customer = customerService.findCustomerProfileByJwt(jwt);
        Wishlist updatedWishlist = wishListService.addProductToWishList(customer, product);

        return ResponseEntity.ok(updatedWishlist);
    }
}
