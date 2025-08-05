package com.zosh.controller;

import com.zosh.model.Cart;
import com.zosh.model.Coupon;
import com.zosh.model.Customer;
import com.zosh.model.User;
import com.zosh.service.CartService;
import com.zosh.service.CouponService;
import com.zosh.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/coupons")
public class ManagerCouponController {

    private final CouponService couponService;

    private final CustomerService customerService;

    private final CartService cartService;

    @PostMapping("/apply")
    public ResponseEntity<Cart> applyCoupon(
            @RequestParam String apply,
            @RequestParam String code,
            @RequestParam double orderValue,
            @RequestHeader("Authorization"
            ) String jwt
    ) throws Exception {
        Customer customer = customerService.findCustomerByJwtToken(jwt);
        Cart cart;

        if(apply.equals("true")){
            cart = couponService.applyCoupon(code, orderValue, customer);
        }
        else {
            cart = couponService.removeCoupon(code, customer);
        }
        return ResponseEntity.ok(cart);
    }


    // Admin operations
    @PostMapping("/admin/create")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<Coupon> createCoupon(@RequestBody Coupon coupon) {

        Coupon createdCoupon = couponService.createCoupon(coupon);
        return ResponseEntity.ok(createdCoupon);
    }

    @DeleteMapping("/admin/delete/{id}")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<?> deleteCoupon(@PathVariable Long id) throws Exception {
        couponService.deleteCoupon(id);
        return ResponseEntity.ok("Coupon deleted successfully");
    }

    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<List<Coupon>> getAllCoupons() {
        List<Coupon> coupons = couponService.findAllCoupons();
        return ResponseEntity.ok(coupons);
    }
}
