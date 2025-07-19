package com.zosh.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String email;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    private String fullName;

    private String mobile;

    @OneToOne(cascade = CascadeType.ALL )
    @JoinColumn(name = "account_id", unique = true,nullable = false)
    private Account account;

    @OneToMany
    private Set<Address> addresses = new HashSet<>();

    @ManyToMany
    @JoinTable(
            name = "customer_coupons",
            joinColumns = @JoinColumn(name = "customerId"),
            inverseJoinColumns = @JoinColumn(name = "couponId")
    )
    @JsonIgnore
    private Set<Coupon> usedCoupons = new HashSet<>();
}

