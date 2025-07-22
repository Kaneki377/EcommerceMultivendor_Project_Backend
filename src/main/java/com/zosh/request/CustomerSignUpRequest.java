package com.zosh.request;

import com.zosh.domain.Gender;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CustomerSignUpRequest {
    @Valid
    private SignUpRequest account;

    @NotBlank(message = "Họ tên không được để trống")
    private String fullName;


    @NotBlank(message = "Số điện thoại không được để trống")
    @Pattern(
            regexp = "^0\\d{9,10}$",
            message = "Số điện thoại không hợp lệ (phải bắt đầu bằng 0 và có 10-11 chữ số)"
    )
    private String mobile;

   // @NotNull(message = "Giới tính không được để trống")
    private Gender gender;

    //@NotNull(message = "Ngày sinh không được để trống")
    private LocalDate dob;
}
