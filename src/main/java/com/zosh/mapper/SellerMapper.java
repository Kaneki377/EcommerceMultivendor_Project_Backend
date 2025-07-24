package com.zosh.mapper;

import com.zosh.model.Address;
import com.zosh.model.BankDetails;
import com.zosh.model.BusinessDetails;
import com.zosh.request.AddressRequest;
import com.zosh.request.BankDetailRequest;
import com.zosh.request.BusinessDetailRequest;

public class SellerMapper {

    public static BankDetails toBankDetails(BankDetailRequest request) {
        BankDetails bankDetails = new BankDetails();
        bankDetails.setAccountNumber(request.getAccountNumber());
        bankDetails.setAccountHolderName(request.getAccountHolderName());
        bankDetails.setIfscCode(request.getIfscCode());
        return bankDetails;
    }

    public static BusinessDetails toBusinessDetails(BusinessDetailRequest request) {
        BusinessDetails businessDetails = new BusinessDetails();
        businessDetails.setBusinessName(request.getBusinessName());
        businessDetails.setBusinessAddress(request.getBusinessAddress());
        businessDetails.setBusinessEmail(request.getBusinessEmail());
        businessDetails.setBusinessMobile(request.getBusinessMobile());
        businessDetails.setLogo(request.getLogo());
        businessDetails.setBanner(request.getBanner());
        return businessDetails;
    }

    public static Address toAddress(AddressRequest req) {
        Address address = new Address();
        address.setStreet(req.getStreet());
        address.setCity(req.getCity());
        address.setState(req.getState());
        address.setLocality(req.getLocality());;
        address.setName(req.getName());;
        address.setMobile(req.getName());;
        address.setPostalCode(req.getPostalCode());
        return address;
    }
}

