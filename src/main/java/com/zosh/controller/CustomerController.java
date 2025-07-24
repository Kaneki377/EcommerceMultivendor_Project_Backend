package com.zosh.controller;

import com.zosh.model.Customer;
import com.zosh.model.Home;
import com.zosh.model.HomeCategory;
import com.zosh.repository.CustomerRepository;
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
    @PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMER')")
    @GetMapping("/users/profile")
    public ResponseEntity<Customer> createUserHandler(@RequestHeader("Authorization") String jwt) throws Exception {

        Customer customer = customerService.findCustomerByJwtToken(jwt);

        return  ResponseEntity.ok().body(customer); //// JSON nếu customer là object
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
        Home home= homeService.creatHomePageData(homeCategories);
        return new ResponseEntity<>(home, HttpStatus.ACCEPTED);
    }
}
