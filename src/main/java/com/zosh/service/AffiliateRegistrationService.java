package com.zosh.service;

import com.zosh.exceptions.KocException;
import com.zosh.exceptions.SellerException;
import com.zosh.model.AffiliateRegistration;

import java.util.List;

public interface AffiliateRegistrationService {

    AffiliateRegistration registerCampaign(Long campaignId, String jwt) throws KocException, SellerException;

    AffiliateRegistration approveRegistration(Long registrationId, String jwt) throws SellerException;

    AffiliateRegistration rejectRegistration(Long registrationId, String jwt) throws SellerException;

    List<AffiliateRegistration> getMyRegistrations(String jwt) throws KocException;

    List<AffiliateRegistration> getRegistrationsForMyCampaigns(String jwt) throws SellerException;
}
