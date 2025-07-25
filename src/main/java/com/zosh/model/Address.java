package com.zosh.model;

import com.zosh.domain.AddressOwnerType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String locality;

    private String street;

    private String city;

    private String state;

    private String postalCode;

    private String mobile;

    @Enumerated(EnumType.STRING)
    private AddressOwnerType ownerType; // "pickup", "shipping", "home", ...

    private Long ownerId;     // ID của chủ sở hữu địa chỉ (Customer, Seller, Store, ...)

}
