package com.zosh.controller;

import com.zosh.model.Customer;

import com.zosh.repository.CustomerRepository;
import com.zosh.request.SignUpRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final CustomerRepository customerRepository;

    @PostMapping("/signup")
    public ResponseEntity<Customer> createCustomerHandler(@RequestBody SignUpRequest req) {
        Customer customer = new Customer();
        customer.setEmail(req.getEmail());
        customer.setFullName(req.getFullName());

        Customer saveCustomer =  customerRepository.save(customer);
        return ResponseEntity.ok().body(saveCustomer);
    }
}
