package com.zosh.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.zosh.domain.Gender;
import com.zosh.model.Customer;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
public class CustomerProfileResponse {
    private Long id;
    private String fullName;
    private String mobile;

    private List<AddressDto> addresses; // thêm danh sách địa chỉ
    private Gender gender;
//    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
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

        // Map từ entity Address sang DTO (để tránh lazy loading hoặc recursion)
        if (customer.getAddresses() != null) {
            this.addresses = customer.getAddresses()
                    .stream()
                    .map(AddressDto::new)
                    .collect(Collectors.toList());
        }
    }
}
