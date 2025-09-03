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
    String kocCode;
    Long customerId;
    String email;
    String facebookLink;
    String instagramLink;
    String tiktokLink;
    String youtubeLink;
}
