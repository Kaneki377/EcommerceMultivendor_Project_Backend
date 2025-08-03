package com.zosh.service.impl;

import com.zosh.config.JwtProvider;
import com.zosh.dto.CustomerProfileResponse;
import com.zosh.exceptions.CustomerException;
import com.zosh.model.Customer;
import com.zosh.repository.CustomerRepository;
import com.zosh.request.UpdateCustomerRequest;
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

    @Override
    public CustomerProfileResponse updateProfile(String jwt, UpdateCustomerRequest request) throws CustomerException {
        try {
            String username = jwtProvider.getUsernameFromJwtToken(jwt);
            Customer customer = findCustomerByUsername(username);

            // Cập nhật các trường từ request DTO

            if (request.getFullName() != null) customer.setFullName(request.getFullName());
            if (request.getMobile() != null) customer.setMobile(request.getMobile());
            if (request.getGender() != null) customer.setGender(request.getGender());
            if (request.getDob() != null) customer.setDob(request.getDob());

            Customer updatedCustomer = customerRepository.save(customer);

            return new CustomerProfileResponse(
                    updatedCustomer.getId(),
                    updatedCustomer.getFullName(),
                    updatedCustomer.getMobile(),
                    updatedCustomer.getGender(),
                    updatedCustomer.getDob(),
                    updatedCustomer.getAccount().getEmail(),
                    updatedCustomer.isKoc()
            );
        } catch (Exception e) {
            throw new CustomerException("Unable to update profile: " + e.getMessage());
        }
    }


}
