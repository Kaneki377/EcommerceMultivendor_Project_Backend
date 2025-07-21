package com.zosh.service;


import com.zosh.exceptions.CustomerException;
import com.zosh.request.CustomerSignUpRequest;
import com.zosh.request.LoginRequest;
import com.zosh.request.SignUpRequest;
import com.zosh.response.AuthResponse;

import javax.swing.*;

public interface AuthService {

    void sentLoginOtp(String email) throws Exception;
    String createUser(CustomerSignUpRequest req) throws CustomerException;
    AuthResponse signIn(LoginRequest req) throws Exception;
}
