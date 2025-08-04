package com.zosh.controller;

import com.zosh.exceptions.CartItemException;
import com.zosh.exceptions.CustomerException;
import com.zosh.exceptions.ProductException;
import com.zosh.model.Cart;
import com.zosh.model.CartItem;
import com.zosh.model.Customer;
import com.zosh.model.Product;
import com.zosh.request.AddItemRequest;
import com.zosh.response.ApiResponse;
import com.zosh.service.CartItemService;
import com.zosh.service.CartService;
import com.zosh.service.CustomerService;
import com.zosh.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;

    private final CartItemService cartItemService;

    private final CustomerService customerService;

    private final ProductService productService;

    @GetMapping
    public ResponseEntity<Cart> findCustomerCartHandler(
            @RequestHeader("Authorization") String jwt
    ) throws CustomerException {
        Customer customer = customerService.findCustomerByJwtToken(jwt);

        Cart cart = cartService.findCustomerCart(customer);

        System.out.println("cart - "+ cart.getCustomer().getAccount().getEmail());

        return new ResponseEntity<Cart>(cart, HttpStatus.OK);
    }

    @PutMapping("/add")
    public ResponseEntity<CartItem> addItemToCart(
            @RequestBody AddItemRequest request,
            @RequestHeader("Authorization") String jwt
    ) throws ProductException, CustomerException {
        Customer customer = customerService.findCustomerByJwtToken(jwt);
        Product product = productService.findProductById(request.getProductId());

        CartItem item = cartService.addCartItem(
                customer, product,
                request.getSize(),
                request.getQuantity());

        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setMessage("Item Added To Cart Successfully");

        return new ResponseEntity<>(item, HttpStatus.ACCEPTED);
    }

    @DeleteMapping("/item/{cartItemId}")
    public ResponseEntity<ApiResponse> deleteCartItemHandler(
            @PathVariable Long cartItemId,
            @RequestHeader("Authorization") String jwt
    ) throws CartItemException, CustomerException {
        Customer customer = customerService.findCustomerByJwtToken(jwt);
        cartItemService.removeCartItem(customer.getId(), cartItemId);

        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setMessage("Item Remove From Cart Successfully");
        apiResponse.setStatus(true);
        return new ResponseEntity<>(apiResponse, HttpStatus.ACCEPTED);
    }

    @PutMapping("/item/{cartItemId}")
    public ResponseEntity<CartItem> updateCartItemHandler(
            @PathVariable Long cartItemId,
            @RequestBody CartItem cartItem,
            @RequestHeader("Authorization") String jwt
    ) throws CartItemException, CustomerException {
        Customer customer = customerService.findCustomerByJwtToken(jwt);

        CartItem updatedCartItem = null;
        if(cartItem.getQuantity() > 0){
            updatedCartItem = cartItemService.updateCartItem(
                    customer.getId(), cartItemId, cartItem);
        }

        return new ResponseEntity<>(updatedCartItem, HttpStatus.ACCEPTED);
    }
}
