package com.zosh.dto;

import com.zosh.domain.AccountStatus;
import com.zosh.model.Address;
import com.zosh.model.BankDetails;
import com.zosh.model.BusinessDetails;
import com.zosh.model.Seller;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SellerProfileDto {
    private Long id;
    private String sellerName;
    private String mobile;
    private String email;
    private BusinessDetails businessDetails;
    private BankDetails bankDetails;
    private Address pickupAddress;
    private String taxCode;
    private boolean emailVerified;
    private AccountStatus accountStatus;

    public static SellerProfileDto fromEntity(Seller seller) {
        return SellerProfileDto.builder()
                .id(seller.getId())
                .sellerName(seller.getSellerName())
                .mobile(seller.getMobile())
                .email(seller.getAccount() != null ? seller.getAccount().getEmail() : null)
                .businessDetails(seller.getBusinessDetails())
                .bankDetails(seller.getBankDetails())
                .pickupAddress(seller.getPickupAddress())
                .taxCode(seller.getTaxCode())
                .emailVerified(seller.isEmailVerified())
                .accountStatus(seller.getAccountStatus())
                .build();
    }
}
