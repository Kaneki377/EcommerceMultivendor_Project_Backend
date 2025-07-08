package com.zosh.service;

import com.zosh.request.SignUpRequest;

import javax.swing.*;

public interface AuthService {

    void sentLoginOtp(String email) throws Exception;
    String createUser(SignUpRequest req) throws Exception;
}
