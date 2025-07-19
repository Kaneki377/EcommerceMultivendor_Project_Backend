package com.zosh.repository;

import com.zosh.domain.AccountStatus;
import com.zosh.model.Seller;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SellerRepository extends JpaRepository<Seller, Long> {
    Seller findByAccount_Email(String accountEmail);

    /*SELECT * FROM seller s
    JOIN account a ON s.account_id = a.id
    WHERE a.email = ?
    LIMIT 1
    */

    List<Seller> findByAccountStatus(AccountStatus accountStatus);
}
