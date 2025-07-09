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
        String email = jwtProvider.getEmailFromJwtToken(jwt);

        return this.findCustomerByEmail(email);
    }

    @Override
    public Customer findCustomerByEmail(String email) throws Exception {
        Customer customer = customerRepository.findByEmail(email);
        if(customer == null){
            throw new Exception("User not found with email - " + email);
        }
        return customer;
    }
}
