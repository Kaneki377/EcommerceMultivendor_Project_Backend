package com.zosh.service.impl;

import com.zosh.domain.AccountStatus;
import com.zosh.domain.USER_ROLE;
import com.zosh.exceptions.LoginException;
import com.zosh.model.Account;
import com.zosh.model.Customer;
import com.zosh.model.Role;
import com.zosh.model.Seller;
import com.zosh.repository.AccountRepository;
import com.zosh.repository.CustomerRepository;
import com.zosh.repository.KocRepository;
import com.zosh.repository.SellerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
public class CustomUserServiceImpl implements UserDetailsService {

    private final CustomerRepository customerRepository;
    private final SellerRepository sellerRepository;
    private final AccountRepository accountRepository;
    private final KocRepository kocRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws LoginException {
        Account account = accountRepository.findByUsername(username);

        if (account == null) {
            throw new LoginException("Account does not exist");
        }

        if (!account.getIsEnabled()) {
            throw new LoginException("Your account has been disabled");
        }
        //Nếu là admin
        if (account.getRole().getName().equals(USER_ROLE.ROLE_MANAGER.name())) {
            return buildUserDetails(account.getUsername(), account.getPassword(), USER_ROLE.ROLE_MANAGER);
        }
        //Kiểm tra xem là Seller hay Customer


            Seller seller = sellerRepository.findByAccount_Username(username);

        if (seller != null) {
            // 1. Chưa verify email
            if (!seller.isEmailVerified()) {
                throw new LoginException("Email unverified account");
            }

            // 2. Nếu vẫn đang pending => chưa được active
            if (seller.getAccountStatus() == AccountStatus.PENDING_VERIFICATION) {
                throw new LoginException("Seller account don't accept active");
            }

            return buildUserDetails(
                    seller.getAccount().getUsername(),
                    seller.getAccount().getPassword(),
                    USER_ROLE.ROLE_SELLER
            );
        }
        // Nếu là Customer hoặc KOC
        Customer customer = customerRepository.findByAccount_Username(username);
        if (customer != null && customer.getAccount() != null) {
            Role role = customer.getAccount().getRole(); // Lấy role thực tế từ DB (ROLE_CUSTOMER hoặc ROLE_KOC)
            return buildUserDetails(customer.getAccount().getUsername(), customer.getAccount().getPassword(), USER_ROLE.valueOf(role.getName()));
        }
        throw new LoginException("No user found with username - " + username);
    }


    //Tạo một object UserDetails (Spring Security dùng để xác thực và phân quyền).

    private UserDetails buildUserDetails(String username, String password,USER_ROLE role) {
        if (role == null) role = USER_ROLE.ROLE_CUSTOMER;

        List<GrantedAuthority> authorityList = new ArrayList<>();
        authorityList.add(new SimpleGrantedAuthority(role.toString()));

        return new org.springframework.security.core.userdetails.User(username, password, authorityList);
    }
}
