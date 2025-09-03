package com.zosh.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.zosh.domain.Gender;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // private String email;
    // @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    // private String password;

    private String fullName;

    private String mobile;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender")
    private Gender gender;

    @Column(name = "dob")
    // @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    private LocalDate dob;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "account_id", unique = true, nullable = false)
    @JsonBackReference
    private Account account;

    @OneToMany
    private Set<Address> addresses = new HashSet<>();

    @ManyToMany
    @JoinTable(name = "customer_coupons", joinColumns = @JoinColumn(name = "customerId"), inverseJoinColumns = @JoinColumn(name = "couponId"))
    @JsonIgnore
    private Set<Coupon> usedCoupons = new HashSet<>();

    private boolean isKoc = false;
}
