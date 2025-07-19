package com.zosh.service;

import com.zosh.model.Role;
import com.zosh.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializationComponent implements CommandLineRunner {

    private final RoleRepository roleRepository;

    @Override
    public void run(String... args) {
        createRoleIfNotExist("ROLE_CUSTOMER");
        createRoleIfNotExist("ROLE_SELLER");
        createRoleIfNotExist("ROLE_MANAGER");
    }

    private void createRoleIfNotExist(String roleName) {
        if (roleRepository.findByName(roleName) == null) {
            Role role = new Role();
            role.setName(roleName);
            roleRepository.save(role);
        }
    }
}
