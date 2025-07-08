package com.zosh.repository;

import com.zosh.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

//extends class interface JpaRepository (CRUD, Sorting , Paging)
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Customer  findByEmail(String email); // SELECT * FROM customer WHERE email = ? LIMIT 1
}
