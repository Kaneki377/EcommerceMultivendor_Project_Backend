package com.zosh.service.impl;

import com.zosh.config.JwtProvider;
import com.zosh.exceptions.UserException;


import com.zosh.model.User;
import com.zosh.repository.UserRepository;
import com.zosh.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;

    @Override
    public User findUserProfileByJwt(String jwtToken) throws UserException {
        String username = jwtProvider.getUsernameFromJwtToken(jwtToken);
        return this.findUserByUsername(username);
    }

    @Override
    public User findUserByEmail(String email) throws UserException {
        User user = userRepository.findByAccount_Email(email);
        if(user == null){
            throw new UserException("Manager not found with email - " + email);
        }
        return user;
    }

    @Override
    public User findUserByUsername(String username) throws UserException {
        User user = userRepository.findByAccount_Username(username);
        if(user == null){
            throw new UserException("Manager not found with username - " + username);
        }
        return user;
    }
}
