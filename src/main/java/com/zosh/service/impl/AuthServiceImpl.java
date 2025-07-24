package com.zosh.service.impl;

import com.zosh.config.JwtProvider;
import com.zosh.domain.USER_ROLE;
import com.zosh.exceptions.CustomerException;
import com.zosh.exceptions.SellerException;
import com.zosh.model.*;
import com.zosh.repository.*;
import com.zosh.request.CustomerSignUpRequest;
import com.zosh.request.LoginRequest;
import com.zosh.request.SignUpRequest;
import com.zosh.response.AuthResponse;
import com.zosh.service.AuthService;
import com.zosh.service.EmailService;
import com.zosh.utils.OtpUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;


@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final CustomerRepository customerRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final CartRepository cartRepository;
    private final JwtProvider jwtProvider;
    private final VerificationCodeRepository verificationCodeRepository;
    private final EmailService emailService;
    private final CustomUserServiceImpl customUserService;
    private final AccountRepository accountRepository;

    @Override
    public void sentSignUpOtp(String email) throws Exception {
        String SIGNING_PREFIX="signin_";

        if(email.startsWith(SIGNING_PREFIX)){
            email=email.substring(SIGNING_PREFIX.length());

            Customer customer = customerRepository.findByAccount_Email(email);
            if(customer == null){
                throw new Exception("Customer not exist with provided email");
            }
        }

        VerificationCode isExist = verificationCodeRepository.findByEmail(email);

        if(isExist != null){
            verificationCodeRepository.delete(isExist);
        }

        String otp = OtpUtils.generateOTP();

        VerificationCode verificationCode = new VerificationCode();
        verificationCode.setOtp(otp);
        verificationCode.setEmail(email);
        verificationCodeRepository.save(verificationCode);

        String subsject = "zosh login/signup otp";
        String text = "your login/signup otp is - " + otp;

        emailService.sendVerificationOtpEmail(email,otp,subsject,text);
    }

    @Override
    @Transactional
    public String createUser(CustomerSignUpRequest req) throws CustomerException {

        VerificationCode verificationCode = verificationCodeRepository.findByEmail(req.getAccount().getEmail());

        Account existingAccountUsername = accountRepository.findByUsername(req.getAccount().getUsername());
        if (existingAccountUsername != null) {
            throw new CustomerException("Username đã tồn tại, vui lòng chọn username khác !");
        }
        Account existingAccountEmail = accountRepository.findByEmail(req.getAccount().getEmail());
        if (existingAccountEmail != null) {
            throw new CustomerException("Email đã tồn tại, vui lòng chọn username khác !");
        }
        if(verificationCode == null || !verificationCode.getOtp().equals(req.getAccount().getOtp())){
            throw new CustomerException("Wrong otp ...");
        }


        // tạo account, customer, cart như thường

        Customer customer = customerRepository.findByAccount_Username(req.getAccount().getUsername());
        if(customer == null){
            Account account = new Account();
            account.setEmail(req.getAccount().getEmail());
            account.setPassword(passwordEncoder.encode(req.getAccount().getPassword()));
            account.setCreatedAt(new Date());             // thêm dòng này
            account.setIsEnabled(true);                  // thêm dòng này
            account.setUsername(req.getAccount().getUsername());

            // Gán Role
            Role role = roleRepository.findByName(USER_ROLE.ROLE_CUSTOMER.name());
            if (role == null) {
                throw new CustomerException("Role không tồn tại!");
            }
            account.setRole(role);

            account = accountRepository.save(account);

            Customer createdCustomer = new Customer();
            createdCustomer.setAccount(account); // Gán account_id cho customer
            createdCustomer.setFullName(req.getFullName());
            createdCustomer.setMobile(req.getMobile());

            customer = customerRepository.save(createdCustomer);
            Cart cart = new Cart();
            cart.setCustomer(customer);
            cartRepository.save(cart);
        }

        List<GrantedAuthority> authorities = new ArrayList<>();

        //Note
        authorities.add(new SimpleGrantedAuthority(
                USER_ROLE.ROLE_CUSTOMER.toString()));


        Authentication authentication = new UsernamePasswordAuthenticationToken(req.getAccount().getUsername(), null, authorities);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        return jwtProvider.generateToken(authentication);

    }


    //xử lý đăng nhập người dùng bằng username/password
    @Override
    public AuthResponse signIn(LoginRequest req) throws Exception {
        String username = req.getUsername();
        String password = req.getPassword();


        //Gọi hàm authenticateWithPassword(...) để xác minh người dùng có tồn tại và username/password có hợp lệ hay không.

        Authentication authentication = authenticateWithPassword(username, password);

        //Thông báo với Spring Security: “Người dùng này đã đăng nhập thành công”
        SecurityContextHolder.getContext().setAuthentication(authentication);

        //Tạo JWT token từ thông tin người dùng.
        String token = jwtProvider.generateToken(authentication);

        AuthResponse authResponse = new AuthResponse();
        authResponse.setJwt(token);
        authResponse.setMessage("Login successful!");

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        String roleName = authorities.isEmpty()?null:authorities.iterator().next().getAuthority();

        authResponse.setRole(USER_ROLE.valueOf(roleName));

        return authResponse;
    }
    //Xác thực bằng otp
    private Authentication authenticate(String username, String otp) throws Exception {
        UserDetails userDetails = customUserService.loadUserByUsername(username);

        if(userDetails == null){
            throw new BadCredentialsException("Invalid username or password");
        }

        VerificationCode verificationCode = verificationCodeRepository.findByEmail(username);

        // verificationCode trong database khác otp user (FE)
        if(verificationCode == null || !verificationCode.getOtp().equals(otp)){
            throw new Exception("Wrong otp !!!");
        }
        return new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.getAuthorities());
    }
    //Xác thực username password
    private Authentication authenticateWithPassword(String username, String rawPassword) throws Exception {
        // Lấy userDetails từ service
        UserDetails userDetails = customUserService.loadUserByUsername(username);

        if (userDetails == null) {
            throw new BadCredentialsException("Tài khoản không tồn tại");
        }

        // Ép về Account để lấy password gốc (nếu bạn custom UserDetails), hoặc lấy password từ userDetails
        String encodedPassword = userDetails.getPassword();

        if (!passwordEncoder.matches(rawPassword, encodedPassword)) {
            throw new BadCredentialsException("Sai mật khẩu");
        }

        return new UsernamePasswordAuthenticationToken(
                userDetails, // Principal
                null, // Credentials
                userDetails.getAuthorities() // Roles
        );
    }
}
