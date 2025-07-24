package com.zosh.service.impl;

import com.zosh.model.Cart;
import com.zosh.model.Coupon;
import com.zosh.model.Customer;
import com.zosh.repository.CartRepository;
import com.zosh.repository.CouponRepository;
import com.zosh.repository.CustomerRepository;
import com.zosh.service.CouponService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CouponServiceImpl implements CouponService {

    private final CouponRepository couponRepository;

    private final CartRepository cartRepository;
    private final CustomerRepository customerRepository;

    @Override
    public Cart applyCoupon(String code, double orderValue, Customer customer) throws Exception {
        Coupon coupon = couponRepository.findByCode(code);

        Cart cart = cartRepository.findByCustomerId(customer.getId());

        if(coupon == null) {
            throw new Exception("Coupon not valid");
        }

        if(customer.getUsedCoupons().contains(coupon)) {
            throw new Exception("Coupon is already used");
        }

        if(orderValue < coupon.getMinimumOrderValue()) {
            throw new Exception("Minimum Order value to use coupon is " + coupon.getMinimumOrderValue());
        }

        //Check expire date cua Coupon
        if(coupon.isActive() && LocalDate.now().isAfter(coupon.getValidityStartDate())
        && LocalDate.now().isBefore(coupon.getValidityEndDate())) {
            customer.getUsedCoupons().add(coupon);
            customerRepository.save(customer);

            double discountedPrice = (cart.getTotalSellingPrice() * coupon.getDiscountPercentage()) / 100;

            cart.setTotalSellingPrice(cart.getTotalSellingPrice() + discountedPrice);
            cart.setCouponCode(code);
            cartRepository.save(cart);
            return cart;
        }
        throw new Exception("Coupon not valid");
    }

    @Override
    public Cart removeCoupon(String code, Customer customer) throws Exception {

        Coupon coupon = couponRepository.findByCode(code);

        if(coupon == null) {
            throw new Exception("Coupon not found...");
        }

        Cart cart = cartRepository.findByCustomerId(customer.getId());
        double discountedPrice = (cart.getTotalSellingPrice() * coupon.getDiscountPercentage()) / 100;

        cart.setTotalSellingPrice(cart.getTotalSellingPrice() + discountedPrice);
        cart.setCouponCode(code);

        return cartRepository.save(cart);
    }

    @Override
    public Coupon findCouponById(Long id) throws Exception {

        return couponRepository.findById(id).orElseThrow(()->
                 new Exception("Coupon not found"));
    }

    @Override
    @PreAuthorize("hasRole ('MANAGER')")
    public Coupon createCoupon(Coupon coupon) {

        return couponRepository.save(coupon);
    }

    @Override
    public List<Coupon> findAllCoupons() {

        return couponRepository.findAll();
    }

    @Override
    @PreAuthorize("hasRole ('MANAGER')")
    public void deleteCoupon(Long id) throws Exception {
        findCouponById(id);
        couponRepository.deleteById(id);
    }
}
