package com.zosh.request;

import com.zosh.domain.Gender;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CustomerSignUpRequest {
    @Valid
    private SignUpRequest account;

    @NotBlank(message = "Họ tên không được để trống")
    private String fullName;


    @NotBlank(message = "Số điện thoại không được để trống")
    private String mobile;

   // @NotNull(message = "Giới tính không được để trống")
    private Gender gender;

    //@NotNull(message = "Ngày sinh không được để trống")
    private LocalDate dob;
}
