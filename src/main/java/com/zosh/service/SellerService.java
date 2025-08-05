package com.zosh.service;

import com.zosh.domain.AccountStatus;
import com.zosh.exceptions.ProductException;
import com.zosh.exceptions.SellerException;
import com.zosh.model.Seller;
import com.zosh.request.SellerSignUpRequest;

import java.util.List;

public interface SellerService {

    Seller getSellerProfile(String jwt) throws SellerException;
    Seller createSeller(SellerSignUpRequest req) throws SellerException;
    Seller getSellerById(Long id) throws SellerException;
    //Seller getSellerByEmail(String email) throws Exception;
    Seller getSellerByUsername(String username) throws SellerException;

    List<Seller> getAllSellers(AccountStatus status);
    Seller updateSeller(Long id, Seller seller) throws SellerException;
    void deleteSeller(Long id) throws SellerException;
    Seller verifyEmail(String email, String otp) throws SellerException;
    Seller updateSellerAccountStatus(Long sellerId, AccountStatus status) throws SellerException;
}
