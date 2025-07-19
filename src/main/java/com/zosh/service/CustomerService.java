package com.zosh.service;

import com.zosh.model.Customer;

public interface CustomerService {
    Customer findCustomerByJwtToken(String jwtToken) throws Exception;
    Customer findCustomerByEmail(String email) throws Exception;
    Customer findCustomerByUsername(String username) throws Exception;
}
