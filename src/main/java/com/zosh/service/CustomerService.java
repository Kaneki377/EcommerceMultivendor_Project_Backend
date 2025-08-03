package com.zosh.service;

import com.zosh.dto.CustomerProfileResponse;
import com.zosh.exceptions.CustomerException;
import com.zosh.model.Customer;
import com.zosh.request.UpdateCustomerRequest;

public interface CustomerService {
    Customer findCustomerByJwtToken(String jwtToken) throws Exception;
    Customer findCustomerByEmail(String email) throws Exception;
    Customer findCustomerByUsername(String username) throws Exception;
    CustomerProfileResponse updateProfile(String jwt, UpdateCustomerRequest request) throws CustomerException;
}
