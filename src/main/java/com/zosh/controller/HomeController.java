package com.zosh.controller;

import com.zosh.response.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {

    @GetMapping
    public ApiResponse HomeControllerHandler(){
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setMessage("Weltome to ecommerce multivendor");
        apiResponse.setMessage("Hello");
        return apiResponse;
    }
}
