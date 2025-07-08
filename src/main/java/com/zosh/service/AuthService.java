package com.zosh.service;


import com.zosh.request.LoginRequest;
import com.zosh.request.SignUpRequest;
import com.zosh.response.AuthResponse;

import javax.swing.*;

public interface AuthService {

    void sentLoginOtp(String email) throws Exception;
    String createUser(SignUpRequest req) throws Exception;
    AuthResponse signIn(LoginRequest req);
}
