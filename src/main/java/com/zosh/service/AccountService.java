package com.zosh.service;

import com.zosh.model.Account;

public interface AccountService {
    Account findByEmail(String email);
    Account saveAccount(Account account);
    Account findByUsername(String username);
}
