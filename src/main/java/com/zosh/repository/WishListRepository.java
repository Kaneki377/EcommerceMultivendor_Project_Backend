package com.zosh.repository;

import com.zosh.model.Wishlist;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WishListRepository extends JpaRepository<Wishlist, Long> {

    Wishlist findByCustomerId (Long customerId);
}
