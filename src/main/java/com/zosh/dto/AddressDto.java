package com.zosh.dto;

import com.zosh.domain.AddressOwnerType;
import com.zosh.model.Address;
import lombok.Data;

@Data
public class AddressDto {
    private Long id;
    private String name;
    private String mobile;
    private String postalCode;
    private String street;
    private String locality;
    private String city;
    private String state;
    private AddressOwnerType ownerType;
    private Long ownerId;

    public AddressDto(Address address) {
        this.id = address.getId();
        this.name = address.getName();
        this.mobile = address.getMobile();
        this.postalCode = address.getPostalCode();
        this.street = address.getStreet();
        this.locality = address.getLocality();
        this.city = address.getCity();
        this.state = address.getState();
        this.ownerType = address.getOwnerType();
        this.ownerId = address.getOwnerId();
    }
}
