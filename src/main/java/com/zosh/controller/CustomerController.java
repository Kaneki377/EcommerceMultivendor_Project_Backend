package com.zosh.controller;

import com.zosh.dto.CustomerProfileResponse;
import com.zosh.exceptions.CustomerException;
import com.zosh.model.Customer;
import com.zosh.model.Home;
import com.zosh.model.HomeCategory;
import com.zosh.repository.CustomerRepository;
import com.zosh.request.UpdateCustomerRequest;
import com.zosh.service.CustomerService;
import com.zosh.service.HomeCategoryService;
import com.zosh.service.HomeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    private final HomeCategoryService homeCategoryService;

    private final HomeService homeService;

    //REST API endpoint GET để lấy thông tin profile của Customer
    @GetMapping("/api/customer/profile")
    @PreAuthorize("hasAnyRole('CUSTOMER','KOC')")
    public ResponseEntity<CustomerProfileResponse> getCustomerProfile(
            @RequestHeader("Authorization") String jwt
    ) throws CustomerException {
        Customer customer = customerService.findCustomerProfileByJwt(jwt);
        return ResponseEntity.ok(new CustomerProfileResponse(customer));
    }
    @PatchMapping("/api/customer/profile")
    @PreAuthorize("hasAnyRole('CUSTOMER','KOC')")
    public ResponseEntity<CustomerProfileResponse> updateProfile(
            @RequestBody UpdateCustomerRequest request,
            @RequestHeader("Authorization") String jwt
    ) {
        CustomerProfileResponse updated = customerService.updateProfile(jwt, request);
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/home-page")
    public ResponseEntity<Home> getHomePageData() {
//        Home homePageData = homeService.getHomePageData();
//        return new ResponseEntity<>(homePageData, HttpStatus.ACCEPTED);
        return null;
    }

    @PostMapping("/home/categories")
    public ResponseEntity<Home> createHomeCategories(
            @RequestBody List<HomeCategory> homeCategories
    ) {
        List<HomeCategory> categories = homeCategoryService.createCategories(homeCategories);
        Home home= homeService.creatHomePageData(categories);
//        Home home= homeService.creatHomePageData(homeCategories);
        return new ResponseEntity<>(home, HttpStatus.ACCEPTED);
    }
}
