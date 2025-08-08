package com.zosh.repository;

import com.zosh.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User,Long> {
    User findByAccount_Email(String email);
    User findByAccount_Username(String username);
}
