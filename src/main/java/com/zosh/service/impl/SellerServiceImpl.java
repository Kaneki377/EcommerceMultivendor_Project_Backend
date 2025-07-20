package com.zosh.service.impl;

import com.zosh.config.JwtProvider;
import com.zosh.domain.AccountStatus;
import com.zosh.domain.USER_ROLE;
import com.zosh.exceptions.SellerException;
import com.zosh.model.Account;
import com.zosh.model.Address;
import com.zosh.model.Seller;
import com.zosh.repository.AccountRepository;
import com.zosh.repository.AddressReposity;
import com.zosh.repository.RoleRepository;
import com.zosh.repository.SellerRepository;
import com.zosh.service.SellerService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SellerServiceImpl implements SellerService {
    private final SellerRepository sellerRepository;
    private final JwtProvider jwtProvider;
    private final PasswordEncoder passwordEncoder;
    private final AddressReposity addressReposity;
    private final AccountRepository accountRepository;
    private final RoleRepository roleRepository;
    @Override
    public Seller getSellerProfile(String jwt) throws Exception {
        String email = jwtProvider.getUsernameFromJwtToken(jwt);
        return this.getSellerByEmail(email);
    }

    @Override
    public Seller createSeller(Seller seller) throws Exception {

        String username = seller.getAccount().getUsername();
        String email = seller.getAccount().getEmail();

        // Kiểm tra trùng username
        if (accountRepository.findByUsername(username) != null) {
            throw new Exception("Username đã tồn tại! Vui lòng dùng username khác");
        }

        // Lưu địa chỉ trước
        Address savedAdress = addressReposity.save(seller.getPickupAddress());

        // 4. Tạo Account mới
        Account account = new Account();
        account.setUsername(username);
        account.setEmail(email);
        account.setPassword(passwordEncoder.encode(seller.getAccount().getPassword()));
        account.setRole(roleRepository.findByName(USER_ROLE.ROLE_CUSTOMER.name()));
        account.setCreatedAt(new Date());
        account.setIsEnabled(true); // vì cần xác thực OTP
        account = accountRepository.save(account);

        Seller newSeller = new Seller();
        newSeller.setAccount(account);
        newSeller.setSellerName(seller.getSellerName());
        newSeller.setPickupAddress(seller.getPickupAddress());
        newSeller.setTaxCode(seller.getTaxCode());
        newSeller.setBankDetails(seller.getBankDetails());
        newSeller.setBusinessDetails(seller.getBusinessDetails());
        return sellerRepository.save(newSeller);
    }

    @Override
    public Seller getSellerById(Long id) throws Exception {
        Optional<Seller> optionalSeller = sellerRepository.findById(id);
        if (optionalSeller.isPresent()) {
            return optionalSeller.get();
        }
        throw new SellerException("Seller not found");
    }

    @Override
    public Seller getSellerByEmail(String email) throws Exception {
        Seller seller = sellerRepository.findByAccount_Email(email);
        if(seller == null){
            throw new Exception("Seller not found ... !");
        }
        return seller;
    }

    @Override
    public List<Seller> getAllSellers(AccountStatus status) {
        return sellerRepository.findByAccountStatus(status);
    }

    @Override
    public Seller updateSeller(Long id, Seller seller) throws SellerException {
        Seller existingSeller = sellerRepository.findById(id)
                .orElseThrow(() ->
                        new SellerException("Seller not found with id " + id));


        if (seller.getSellerName() != null) {
            existingSeller.setSellerName(seller.getSellerName());
        }
        if (seller.getMobile() != null) {
            existingSeller.setMobile(seller.getMobile());
        }
        if (seller.getAccount().getEmail() != null) {
            existingSeller.getAccount().setEmail(seller.getAccount().getEmail());
        }

        if (seller.getBusinessDetails() != null
                && seller.getBusinessDetails().getBusinessName() != null
        ) {

            existingSeller.getBusinessDetails().setBusinessName(
                    seller.getBusinessDetails().getBusinessName()
            );
        }

        if (seller.getBankDetails() != null
                && seller.getBankDetails().getAccountHolderName() != null
                && seller.getBankDetails().getIfscCode() != null
                && seller.getBankDetails().getAccountNumber() != null
        ) {

            existingSeller.getBankDetails().setAccountHolderName(
                    seller.getBankDetails().getAccountHolderName()
            );
            existingSeller.getBankDetails().setAccountNumber(
                    seller.getBankDetails().getAccountNumber()
            );
            existingSeller.getBankDetails().setIfscCode(
                    seller.getBankDetails().getIfscCode()
            );
        }
        if (seller.getPickupAddress() != null
                && seller.getPickupAddress().getStreet() != null
                && seller.getPickupAddress().getMobile() != null
                && seller.getPickupAddress().getCity() != null
                && seller.getPickupAddress().getState() != null
        ) {
            existingSeller.getPickupAddress()
                    .setStreet(seller.getPickupAddress().getStreet());
            existingSeller.getPickupAddress().setCity(seller.getPickupAddress().getCity());
            existingSeller.getPickupAddress().setState(seller.getPickupAddress().getState());
            existingSeller.getPickupAddress().setMobile(seller.getPickupAddress().getMobile());
            existingSeller.getPickupAddress().setPostalCode(seller.getPickupAddress().getPostalCode());
        }
        if (seller.getTaxCode() != null) {
            existingSeller.setTaxCode(seller.getTaxCode());
        }


        return sellerRepository.save(existingSeller);
    }

    @Override
    public void deleteSeller(Long id) throws SellerException {
        if (sellerRepository.existsById(id)) {
            sellerRepository.deleteById(id);
        } else {
            throw new SellerException("Seller not found with id " + id);
        }
    }

    @Override
    public Seller verifyEmail(String email, String otp) throws Exception {
        Seller seller = this.getSellerByEmail(email);
        seller.setEmailVerified(true);
        return sellerRepository.save(seller);
    }

    @Override
    public Seller updateSellerAccountStatus(Long sellerId, AccountStatus status) throws Exception {
        Seller seller = this.getSellerById(sellerId);
        seller.setAccountStatus(status);
        return sellerRepository.save(seller);
    }
}
