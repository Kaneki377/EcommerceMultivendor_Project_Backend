package com.zosh.service;


import com.zosh.exceptions.CustomerException;
import com.zosh.exceptions.UserException;

import com.zosh.model.User;


public interface UserService {
    User findUserProfileByJwt(String jwtToken) throws UserException;
    User findUserByEmail(String email) throws UserException;
    User findUserByUsername(String username) throws UserException;
}
