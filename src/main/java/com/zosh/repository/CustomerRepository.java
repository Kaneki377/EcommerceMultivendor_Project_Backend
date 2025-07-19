package com.zosh.repository;

import com.zosh.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

//extends class interface JpaRepository (CRUD, Sorting , Paging)
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    /*  SELECT c.*
        FROM customer c
        JOIN account a ON c.account_id = a.id
        WHERE a.email = ?;
     */
    Customer  findByAccount_Email(String email);

    Customer  findByAccount_Username(String username);
}
