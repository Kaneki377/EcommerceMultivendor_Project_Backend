package com.zosh.service;

import com.zosh.dto.CustomerProfileResponse;
import com.zosh.exceptions.CustomerException;
import com.zosh.model.Customer;
import com.zosh.request.UpdateCustomerRequest;

public interface CustomerService {
    Customer findCustomerProfileByJwt(String jwtToken) throws CustomerException;
    Customer findCustomerByEmail(String email) throws CustomerException;
    Customer findCustomerByUsername(String username) throws CustomerException;
    CustomerProfileResponse updateProfile(String jwt, UpdateCustomerRequest request) throws CustomerException;
}
