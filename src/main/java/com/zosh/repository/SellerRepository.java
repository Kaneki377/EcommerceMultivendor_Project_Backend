package com.zosh.repository;

import com.zosh.domain.AccountStatus;
import com.zosh.model.Seller;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SellerRepository extends JpaRepository<Seller, Long> {
    /*SELECT * FROM seller s
      JOIN account a ON s.account_id = a.id
      WHERE a.username = ?
      LIMIT 1
    */
    Seller findByAccount_Username(String accountUsername);
    Seller findByAccount_Email(String accountEmail);
    Page<Seller> findByAccountStatus(AccountStatus status, Pageable pageable);
}
