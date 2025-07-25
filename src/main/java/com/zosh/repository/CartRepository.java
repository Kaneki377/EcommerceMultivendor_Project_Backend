package com.zosh.repository;

import com.zosh.model.Cart;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartRepository extends JpaRepository<Cart,Long> {

    Cart findByCustomerId(long customerId);
}
