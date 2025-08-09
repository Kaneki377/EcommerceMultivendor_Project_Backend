package com.zosh.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.zosh.domain.Gender;
import lombok.Data;

import java.time.LocalDate;
@Data
public class UpdateCustomerRequest {
    private String fullName;
    private String mobile;
    private Gender gender;

//    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    private LocalDate dob;
}
