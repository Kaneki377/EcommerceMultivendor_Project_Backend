package com.zosh.account;

import com.zosh.model.Account;
import com.zosh.repository.AccountRepository;
import com.zosh.service.AccountService;
import com.zosh.service.impl.AccountServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class AccountServiceImplTest {

    @Autowired
    private AccountRepository accountRepository;

    private AccountService accountService;

    @Test
    void testSaveAndFindByEmail() {
        accountService = new AccountServiceImpl(accountRepository);

        Account account = new Account();
        account.setUsername("user1");
        account.setEmail("test@example.com");
        account.setPassword("123456");

        accountService.saveAccount(account);

        Account result = accountService.findByEmail("test@example.com");

        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("user1");
    }

    @Test
    void testFindByUsername() {
        accountService = new AccountServiceImpl(accountRepository);

        Account account = new Account();
        account.setUsername("user2");
        account.setEmail("user2@example.com");
        account.setPassword("123456");

        accountService.saveAccount(account);

        Account result = accountService.findByUsername("user2@example.com");

        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("user2");
    }
}
