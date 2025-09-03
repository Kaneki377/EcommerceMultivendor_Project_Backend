package com.zosh.service;

import com.zosh.dto.AffiliateRegistrationResponse;
import com.zosh.dto.KocRegistrationDto;
import com.zosh.dto.RegistrationApprovalResponse;
import com.zosh.exceptions.KocException;
import com.zosh.exceptions.SellerException;
import com.zosh.model.AffiliateRegistration;

import java.util.List;

public interface AffiliateRegistrationService {

    AffiliateRegistration registerCampaign(Long campaignId, String jwt) throws KocException, SellerException;

    RegistrationApprovalResponse approveRegistration(Long registrationId, String jwt) throws SellerException;

    RegistrationApprovalResponse rejectRegistration(Long registrationId, String jwt) throws SellerException;

    List<AffiliateRegistrationResponse> getMyRegistrations(String jwt) throws KocException;

    List<KocRegistrationDto> getRegistrationsForMyCampaigns(String jwt) throws SellerException;

}
