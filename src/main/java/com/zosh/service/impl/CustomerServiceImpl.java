package com.zosh.service.impl;

import com.zosh.config.JwtProvider;
import com.zosh.model.Customer;
import com.zosh.repository.CustomerRepository;
import com.zosh.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final JwtProvider jwtProvider;

    @Override
    public Customer findCustomerByJwtToken(String jwt) throws Exception {

        String username = jwtProvider.getUsernameFromJwtToken(jwt);
        return this.findCustomerByUsername(username);
    }

    @Override
    public Customer findCustomerByEmail(String email) throws Exception {
        Customer customer = customerRepository.findByAccount_Email(email);
        if(customer == null){
            throw new Exception("Customer not found with email - " + email);
        }
        return customer;
    }

    @Override
    public Customer findCustomerByUsername(String username) throws Exception {
        Customer customer = customerRepository.findByAccount_Username(username);
        if(customer == null){
                throw new Exception("Customer not found with username - " + username);
        }
        return customer;
    }


}
