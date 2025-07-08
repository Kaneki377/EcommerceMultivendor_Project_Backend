package com.zosh.service.impl;

import com.zosh.config.JwtProvider;
import com.zosh.domain.USER_ROLE;
import com.zosh.model.Cart;
import com.zosh.model.Customer;
import com.zosh.repository.CartRepository;
import com.zosh.repository.CustomerRepository;
import com.zosh.request.SignUpRequest;
import com.zosh.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;


@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;
    private final CartRepository cartRepository;
    private final JwtProvider jwtProvider;
    @Override
    public String createUser(SignUpRequest req) {

        Customer customer = customerRepository.findByEmail(req.getEmail());

        if(customer == null){
            Customer createdCustomer = new Customer();
            createdCustomer.setEmail(req.getEmail());
            createdCustomer.setFullName(req.getFullName());
            createdCustomer.setMobile("0xxxxxxxxx");
            //createdCustomer.setRole(USER_ROLE.ROLE_CUSTOMER);
            createdCustomer.setPassword(passwordEncoder.encode(req.getOtp()));

            customer = customerRepository.save(createdCustomer);

            Cart cart = new Cart();
            cart.setCustomer(customer);
            cartRepository.save(cart);
        }

        List<GrantedAuthority> authorities = new ArrayList<>();

        //Note
        authorities.add(new SimpleGrantedAuthority(
                USER_ROLE.ROLE_CUSTOMER.toString()));


        Authentication authentication = new UsernamePasswordAuthenticationToken(req.getEmail(), null, authorities);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        return jwtProvider.generateToken(authentication);

    }
}
