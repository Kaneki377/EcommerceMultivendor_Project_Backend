package com.zosh.repository;

import com.zosh.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleRepository extends JpaRepository<Role,Integer> { // //Thao tác với entity Role và có kiểu khóa chính là Integer
    Role findByName(String name);
}
