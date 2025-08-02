package com.zosh.dto;

import com.zosh.domain.RegistrationStatus;
import lombok.AllArgsConstructor;
import lombok.Data;


@AllArgsConstructor
@Data
public class RegistrationApprovalResponse {
    private Long registrationId;
    private String campaignCode;
    private String campaignName;
    private String kocId;
    private RegistrationStatus status;

}
