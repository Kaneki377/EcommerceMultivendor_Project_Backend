package com.zosh.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.zosh.domain.Gender;
import com.zosh.model.Customer;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

@Data
@AllArgsConstructor
public class CustomerProfileResponse {
    private Long id;
    private String fullName;
    private String mobile;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    private Gender gender;
    private LocalDate dob;
    private String email;
    private boolean isKoc;
    // Constructor từ entity Customer
    public CustomerProfileResponse(Customer customer) {
        this.id = customer.getId();
        this.fullName = customer.getFullName();
        this.mobile = customer.getMobile();
        this.gender = customer.getGender();
        this.dob = customer.getDob();
        this.email = customer.getAccount() != null ? customer.getAccount().getEmail() : null;
        this.isKoc = customer.isKoc();
    }
}
