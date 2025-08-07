package com.zosh.service;

import com.zosh.exceptions.CouponNotValidException;
import com.zosh.model.Cart;
import com.zosh.model.Coupon;
import com.zosh.model.Customer;

import java.util.List;

public interface CouponService {

    Cart applyCoupon(String code, double orderValue, Customer customer) throws Exception;

    Cart removeCoupon(String code, Customer customer) throws CouponNotValidException;

    Coupon findCouponById(Long id) throws Exception;

    Coupon createCoupon(Coupon coupon);

    List<Coupon> findAllCoupons();

    void deleteCoupon(Long id) throws Exception;
}
