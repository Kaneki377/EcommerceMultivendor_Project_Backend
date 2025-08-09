package com.zosh.dto;

import com.zosh.domain.AccountStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class KocDto {
    Long id;
    String name;
    AccountStatus accountStatus;
    Long customerId;
    String email;
}
