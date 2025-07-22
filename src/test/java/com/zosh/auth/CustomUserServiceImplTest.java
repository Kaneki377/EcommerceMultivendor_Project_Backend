package com.zosh.auth;

import com.zosh.domain.USER_ROLE;
import com.zosh.model.Account;
import com.zosh.model.Customer;
import com.zosh.model.Seller;
import com.zosh.repository.AccountRepository;
import com.zosh.repository.CustomerRepository;
import com.zosh.repository.SellerRepository;
import com.zosh.service.impl.CustomUserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

public class CustomUserServiceImplTest {

    private CustomerRepository customerRepository;
    private SellerRepository sellerRepository;
    private AccountRepository accountRepository;
    private CustomUserServiceImpl customUserService;

    @BeforeEach
    void setup() {
        customerRepository = mock(CustomerRepository.class);
        sellerRepository = mock(SellerRepository.class);
        accountRepository = mock(AccountRepository.class);

        customUserService = new CustomUserServiceImpl(customerRepository, sellerRepository, accountRepository);
    }

    @Test
    void testLoadUserByUsername_WithSellerVerified() {
        String username = "seller1";

        Account acc = new Account();
        acc.setUsername(username);
        acc.setPassword("hashed-password");
        acc.setIsEnabled(true);

        Seller seller = new Seller();
        seller.setAccount(acc);
        seller.setEmailVerified(true);

        when(accountRepository.findByUsername(username)).thenReturn(acc);
        when(sellerRepository.findByAccount_Username(username)).thenReturn(seller);

        UserDetails userDetails = customUserService.loadUserByUsername(username);

        assertThat(userDetails.getUsername()).isEqualTo(username);
        assertThat(userDetails.getAuthorities()).anyMatch(
                a -> a.getAuthority().equals(USER_ROLE.ROLE_SELLER.toString())
        );
    }

    @Test
    void testLoadUserByUsername_WithCustomer() {
        String username = "customer1";

        Account acc = new Account();
        acc.setUsername(username);
        acc.setPassword("hashed-password");
        acc.setIsEnabled(true);

        Customer customer = new Customer();
        customer.setAccount(acc);

        when(accountRepository.findByUsername(username)).thenReturn(acc);
        when(sellerRepository.findByAccount_Username(username)).thenReturn(null);
        when(customerRepository.findByAccount_Username(username)).thenReturn(customer);

        UserDetails userDetails = customUserService.loadUserByUsername(username);

        assertThat(userDetails.getUsername()).isEqualTo(username);
        assertThat(userDetails.getAuthorities()).anyMatch(
                a -> a.getAuthority().equals(USER_ROLE.ROLE_CUSTOMER.toString())
        );
    }

    @Test
    void testLoadUserByUsername_WithDisabledAccount() {
        String username = "disabled";

        Account acc = new Account();
        acc.setUsername(username);
        acc.setIsEnabled(false);

        when(accountRepository.findByUsername(username)).thenReturn(acc);

        assertThatThrownBy(() -> customUserService.loadUserByUsername(username))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("vô hiệu");
    }

    @Test
    void testLoadUserByUsername_WithSellerNotVerified_ShouldThrow() {
        String username = "seller2";

        Account acc = new Account();
        acc.setUsername(username);
        acc.setPassword("123");
        acc.setIsEnabled(true);

        Seller seller = new Seller();
        seller.setAccount(acc);
        seller.setEmailVerified(false); // chưa xác minh

        when(accountRepository.findByUsername(username)).thenReturn(acc);
        when(sellerRepository.findByAccount_Username(username)).thenReturn(seller);

        assertThatThrownBy(() -> customUserService.loadUserByUsername(username))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessageContaining("chưa xác minh");
    }

    @Test
    void testLoadUserByUsername_AccountNotFound_ShouldThrow() {
        String username = "unknown";

        when(accountRepository.findByUsername(username)).thenReturn(null);

        assertThatThrownBy(() -> customUserService.loadUserByUsername(username))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("không tồn tại");
    }
}
