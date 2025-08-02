package com.zosh.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class BankDetailRequest {
    @NotBlank(message = "Số tài khoản không được để trống")
    private String accountNumber;

    @NotBlank(message = "Tên chủ tài khoản không được để trống")
    private String accountHolderName;


    private String bankName;
}
