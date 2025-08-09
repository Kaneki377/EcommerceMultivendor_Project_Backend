package com.zosh.service.impl;

import com.zosh.config.JwtProvider;
import com.zosh.domain.AccountStatus;
import com.zosh.domain.USER_ROLE;
import com.zosh.exceptions.CustomerException;
import com.zosh.exceptions.InvalidRoleLoginException;
import com.zosh.exceptions.SellerException;
import com.zosh.model.*;
import com.zosh.repository.*;
import com.zosh.request.CustomerSignUpRequest;
import com.zosh.request.LoginAdminRequest;
import com.zosh.request.LoginRequest;
import com.zosh.request.SignUpRequest;
import com.zosh.response.AuthResponse;
import com.zosh.service.AuthService;
import com.zosh.service.EmailService;
import com.zosh.utils.OtpUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;


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
    private final SellerRepository sellerRepository;

    @Override
    public void sentSignUpOtp(String email) throws Exception {
        String SIGNING_PREFIX="signin_";
        Date now = new Date();
        Date expiresAt = new Date(now.getTime() + 10 * 60 * 1000); //10p

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
        verificationCode.setCreatedAt(now);
        verificationCode.setExpiresAt(expiresAt);
        verificationCodeRepository.save(verificationCode);

        String subsject = "Zonix Mall login/signup otp";
        String text = "your login/signup otp is - " + otp;

        emailService.sendVerificationOtpEmail(email,otp,subsject,text);
    }

    @Override
    @Transactional
    public String createCustomer(CustomerSignUpRequest req) throws CustomerException {
        VerificationCode verificationCode = verificationCodeRepository.findByEmail(req.getAccount().getEmail());

        Account existingAccountUsername = accountRepository.findByUsername(req.getAccount().getUsername());
        if (existingAccountUsername != null) {
            throw new CustomerException("The username is already taken. Please choose another one.");
        }

        Account existingAccountEmail = accountRepository.findByEmail(req.getAccount().getEmail());
        if (existingAccountEmail != null) {
            throw new CustomerException("An account with this email already exists. Please use a different email.");
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
                throw new CustomerException("Role does not exist!");
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

        // 1. Xác thực tài khoản
        Authentication authentication = authenticateWithPassword(username, password);

        // 2. Set vào SecurityContext
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 3. Kiểm tra tài khoản có phải Customer hay không
        Customer customer = customerRepository.findByAccount_Username(username);
        if (customer == null) {
            throw new InvalidRoleLoginException("Only Customer or KOC accounts are allowed to log in");
        }

        // 4. Tạo JWT
        String token = jwtProvider.generateToken(authentication);

        // 5. Tạo AuthResponse
        AuthResponse authResponse = new AuthResponse();
        authResponse.setJwt(token);
        authResponse.setMessage("Login successful!");
        authResponse.setRole(customer.isKoc() ? USER_ROLE.ROLE_KOC : USER_ROLE.ROLE_CUSTOMER);

        return authResponse;
    }

    @Override
    public AuthResponse loginSeller(LoginRequest req) throws Exception {
        String username = req.getUsername();
        String password = req.getPassword();

        // 1. Xác thực username/password
        Authentication authentication = authenticateWithPassword(username, password);

        // 2. Set context
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 3. Kiểm tra xem có phải tài khoản Seller hay không
        Seller seller = sellerRepository.findByAccount_Username(username);
        if (seller == null) {
            throw new InvalidRoleLoginException("Only Seller accounts are allowed to log in");
        }

        if ( !seller.isEmailVerified()) {
            throw new InvalidRoleLoginException("Email unverified account");
        }

        if (seller.getAccountStatus() == AccountStatus.PENDING_VERIFICATION) {
            throw new InvalidRoleLoginException("Seller account is not active yet");
        }

        // 4. Tạo JWT
        String token = jwtProvider.generateToken(authentication);

        // 5. Trả về AuthResponse
        AuthResponse res = new AuthResponse();
        res.setJwt(token);
        res.setMessage("Login successful!");
        res.setRole(USER_ROLE.ROLE_SELLER);

        return res;
    }

    @Override
    public AuthResponse loginAdmin(LoginAdminRequest req) throws Exception {
        String username = req.getUsername();
        String password = req.getPassword();
        String email = req.getEmail();
        String otp = req.getOtp();

        // B1: Kiểm tra tồn tại tài khoản email & OTP hợp lệ
        VerificationCode verificationCode = verificationCodeRepository.findByEmail(email);
        if (verificationCode == null || !verificationCode.getOtp().equals(otp)) {
            throw new  ResponseStatusException(HttpStatus.BAD_REQUEST,"Wrong OTP  !!!");
        }
        Date now = new Date();
        if (now.after(verificationCode.getExpiresAt())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "OTP is expired");
        }
        // B2: Xác thực username/password
        Authentication authentication = authenticateWithPassword(username, password);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // B3: Kiểm tra vai trò ROLE_MANAGER
        Account account = accountRepository.findByUsername(username);
        if (account == null || !account.getRole().getName().equals("ROLE_MANAGER")) {
            throw new InvalidRoleLoginException("Only Admin can login here.");
        }

        // Xoá OTP sau khi dùng
        verificationCodeRepository.delete(verificationCode);

        // B4: Tạo JWT token
        String token = jwtProvider.generateToken(authentication);

        AuthResponse authResponse = new AuthResponse();
        authResponse.setJwt(token);
        authResponse.setMessage("Login successful!");
        authResponse.setRole(USER_ROLE.ROLE_MANAGER);

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
            throw new BadCredentialsException("Account doesn't exist");
        }

        // Ép về Account để lấy password gốc (nếu bạn custom UserDetails), hoặc lấy password từ userDetails
        String encodedPassword = userDetails.getPassword();

        if (!passwordEncoder.matches(rawPassword, encodedPassword)) {
            throw new BadCredentialsException("Wrong password");
        }

        return new UsernamePasswordAuthenticationToken(
                userDetails, // Principal
                null, // Credentials
                userDetails.getAuthorities() // Roles
        );
    }
}
