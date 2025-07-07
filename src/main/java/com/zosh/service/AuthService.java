package com.zosh.service;

import com.zosh.request.SignUpRequest;

public interface AuthService {
    String createUser(SignUpRequest req);
}
