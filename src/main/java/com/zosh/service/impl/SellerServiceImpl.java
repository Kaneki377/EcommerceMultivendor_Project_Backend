package com.zosh.service.impl;

import com.zosh.config.JwtProvider;
import com.zosh.domain.AccountStatus;
import com.zosh.domain.USER_ROLE;
import com.zosh.exceptions.SellerException;
import com.zosh.mapper.SellerMapper;
import com.zosh.model.Account;
import com.zosh.model.Address;
import com.zosh.model.Seller;
import com.zosh.repository.AccountRepository;
import com.zosh.repository.AddressReposity;
import com.zosh.repository.RoleRepository;
import com.zosh.repository.SellerRepository;
import com.zosh.request.SellerSignUpRequest;
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
        String username = jwtProvider.getUsernameFromJwtToken(jwt);
        return this.getSellerByUsername(username);
    }

    @Override
    public Seller createSeller(SellerSignUpRequest req) throws Exception {
    // Convert từ req sang Seller + Account + BusinessDetail + BankDetail + Address
        // 1. Lấy username và email từ request
        String username = req.getAccount().getUsername();
        String email = req.getAccount().getEmail();

        // 2. Kiểm tra trùng username
        if (accountRepository.findByUsername(username) != null) {
            throw new Exception("Username đã tồn tại! Vui lòng dùng username khác");
        }

        // 3. Lưu địa chỉ nhận hàng
        Address pickupAddress = SellerMapper.toAddress(req.getPickupAddress());
        Address savedAddress = addressReposity.save(pickupAddress);

        // 4. Tạo Account mới
        Account account = new Account();
        account.setUsername(username);
        account.setEmail(email);
        account.setPassword(passwordEncoder.encode(req.getAccount().getPassword()));
        account.setRole(roleRepository.findByName(USER_ROLE.ROLE_SELLER.name()));
        account.setCreatedAt(new Date());
        account.setIsEnabled(true);
        account = accountRepository.save(account);

        Seller newSeller = new Seller();
        newSeller.setAccount(account);
        newSeller.setSellerName(req.getSellerName());
        newSeller.setMobile(req.getMobile());
        newSeller.setPickupAddress(savedAddress);
        newSeller.setTaxCode(req.getTaxCode());
        newSeller.setBankDetails(SellerMapper.toBankDetails(req.getBankDetails()));
        newSeller.setBusinessDetails(SellerMapper.toBusinessDetails(req.getBusinessDetails()));
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

//    @Override
//    public Seller getSellerByEmail(String email) throws Exception {
//        Seller seller = sellerRepository.findByAccount_Email(email);
//        if(seller == null){
//            throw new Exception("Seller not found ... !");
//        }
//        return seller;
//    }

    @Override
    public Seller getSellerByUsername(String username) throws Exception {
        Seller seller = sellerRepository.findByAccount_Username(username);
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
        if (seller.getAccount() != null && seller.getAccount().getEmail() != null) {
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
    public Seller verifyEmail(String username, String otp) throws Exception {
        Seller seller = this.getSellerByUsername(username);
        if (seller == null) {
            throw new SellerException("Không tìm thấy seller.");
        }
        //note
        if (seller.getAccountStatus() == AccountStatus.PENDING_VERIFICATION) {
            seller.setAccountStatus(AccountStatus.ACTIVE);
        }

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
