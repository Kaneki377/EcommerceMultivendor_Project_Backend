package com.zosh.service.impl;

import com.zosh.domain.USER_ROLE;
import com.zosh.model.Customer;
import com.zosh.model.Seller;
import com.zosh.repository.CustomerRepository;
import com.zosh.repository.SellerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
public class CustomUserServiceImpl implements UserDetailsService {

    private final CustomerRepository customerRepository;
    private final SellerRepository sellerRepository;
    private static final String SELLER_PREFIX = "seller_";

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        //Kiểm tra username , Nếu không phải seller, thì xử lý như Customer:
        if(username.startsWith(SELLER_PREFIX)){
            String actualUsername = username.substring(SELLER_PREFIX.length());
            Seller seller = sellerRepository.findByEmail(actualUsername);

            if(seller!=null){
                return buildUserDetails(seller.getEmail(),seller.getPassword(),USER_ROLE.ROLE_SELLER);
            }
        }else{
            Customer customer = customerRepository.findByEmail(username);
            if(customer != null){
                return buildUserDetails(customer.getEmail(),customer.getPassword(),USER_ROLE.ROLE_CUSTOMER);
            }
        }
        throw new UsernameNotFoundException("Customer or Seller not found with email - " + username);
    }


    //Tạo một object UserDetails (Spring Security dùng để xác thực và phân quyền).

    private UserDetails buildUserDetails(String email, String password,USER_ROLE role) {
        if (role == null) role = USER_ROLE.ROLE_CUSTOMER;

        List<GrantedAuthority> authorityList = new ArrayList<>();
        authorityList.add(new SimpleGrantedAuthority("ROLE_"+role));

        return new org.springframework.security.core.userdetails.User(email, password, authorityList);
    }
}
