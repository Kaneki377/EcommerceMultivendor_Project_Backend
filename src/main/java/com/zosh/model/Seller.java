package com.zosh.model;

import com.zosh.domain.AccountStatus;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class Seller {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String sellerName;

    private String mobile;

    //@Column(unique = true, nullable = false)
   // private String email;

    //private String password;
    @OneToOne(cascade = CascadeType.ALL )
    @JoinColumn(name = "account_id", unique = true,nullable = false)
    private Account account;

    @Embedded
    private BusinessDetails businessDetails = new BusinessDetails();

    @Embedded
    private BankDetails bankDetails = new BankDetails();

    @OneToOne(cascade = CascadeType.ALL)
    private Address pickupAddress = new Address();

    private String taxCode;

    private boolean isEmailVerified = false;

    private AccountStatus accountStatus = AccountStatus.PENDING_VERIFICATION;
}
