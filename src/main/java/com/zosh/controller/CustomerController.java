package com.zosh.controller;

import com.zosh.model.Customer;
import com.zosh.repository.CustomerRepository;
import com.zosh.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class CustomerController {
    private final CustomerService customerService;


    //REST API endpoint GET để lấy thông tin profile của Customer
    @PreAuthorize("hasRole('ROLE_CUSTOMER')")
    @GetMapping("/users/profile")
    public ResponseEntity<Customer> createUserHandler(@RequestHeader("Authorization") String jwt) throws Exception {

        Customer customer = customerService.findCustomerByJwtToken(jwt);

        return  ResponseEntity.ok().body(customer); //// JSON nếu customer là object
    }
}
