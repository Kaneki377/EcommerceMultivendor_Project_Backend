package com.zosh.service;

import com.zosh.domain.USER_ROLE;
import com.zosh.model.Account;
import com.zosh.model.Role;
import com.zosh.model.User;
import com.zosh.repository.AccountRepository;
import com.zosh.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializationComponent implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        createRoleIfNotExist("ROLE_CUSTOMER");
        createRoleIfNotExist("ROLE_SELLER");
        createRoleIfNotExist("ROLE_MANAGER");
        createRoleIfNotExist("ROLE_KOC");
        initializeAdminUser();
    }

    private void createRoleIfNotExist(String roleName) {
        if (roleRepository.findByName(roleName) == null) {
            Role role = new Role();
            role.setName(roleName);
            roleRepository.save(role);
        }
    }
    private void initializeAdminUser() {
        String adminUsername = "admin";

        if (accountRepository.findByUsername(adminUsername)==null) {
            Account adminUser = new Account();

            adminUser.setPassword(passwordEncoder.encode("phat29122003"));
            adminUser.setEmail("phamtanphat469@gmail.com");
            adminUser.setUsername(adminUsername);
            adminUser.setIsEnabled(true);
            Role adminRole = roleRepository.findByName("ROLE_MANAGER");
            adminUser.setRole(adminRole);

            Account admin=accountRepository.save(adminUser);
        }
    }
}
