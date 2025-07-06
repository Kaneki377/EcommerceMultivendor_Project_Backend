package com.zosh.repository;

import com.zosh.modal.Role;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleRepository extends CrudRepository<Role,Integer> { // //Thao tác với entity Role và có kiểu khóa chính là Integer


}
