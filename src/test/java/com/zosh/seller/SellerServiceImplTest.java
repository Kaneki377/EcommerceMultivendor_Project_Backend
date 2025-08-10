package com.zosh.seller;

import com.zosh.domain.AccountStatus;
import com.zosh.model.Account;

import com.zosh.model.Seller;
import com.zosh.repository.SellerRepository;
import com.zosh.request.*;
import com.zosh.service.SellerService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@Transactional
@Configuration
public class SellerServiceImplTest {

    @Autowired
    private SellerService sellerService;

    @Autowired
    private SellerRepository sellerRepository;

    @Test
    void testCreateSellerAndFetch() throws Exception {
        // Tạo dữ liệu request
        SellerSignUpRequest request = new SellerSignUpRequest();
        SignUpRequest acc = new SignUpRequest();
        acc.setUsername("test_seller");
        acc.setEmail("seller@example.com");
        acc.setPassword("pass123");
        request.setAccount(acc);

        BusinessDetailRequest businessDetailRequest = new BusinessDetailRequest();
        businessDetailRequest.setBusinessAddress("12 Disctric 1");
        businessDetailRequest.setBusinessName("Test Seller");
        businessDetailRequest.setBusinessEmail("seller@example.com");
        businessDetailRequest.setBusinessMobile("0123456789");
        businessDetailRequest.setLogo("logo");
        businessDetailRequest.setBanner("banner");

        request.setBusinessDetails(businessDetailRequest);

        BankDetailRequest  bankDetailRequest = new BankDetailRequest();
        bankDetailRequest.setAccountHolderName("Test Seller");
        bankDetailRequest.setAccountNumber("09931232123");
        bankDetailRequest.setBankName("1234");

        request.setBankDetails(bankDetailRequest);

        @Valid @NotNull(message = "Địa chỉ lấy hàng không được để trống") AddressRequest addr = new AddressRequest();
        addr.setStreet("123 Main");
        addr.setCity("HCM");
        addr.setState("HCM");
        addr.setPostalCode("70000");
        addr.setMobile("0123456789");
        request.setPickupAddress(addr);

        request.setMobile("0123456789");
        request.setSellerName("Test Seller");
        request.setTaxCode("TAXCODE");

        Seller seller = sellerService.createSeller(request);

        assertNotNull(seller.getId());
        assertEquals("Test Seller", seller.getSellerName());
    }

    @Test
    void testUpdateSellerStatus() throws Exception {
        Seller seller = new Seller();
        Account acc = new Account();
        acc.setUsername("status_user");
        acc.setEmail("email@a.com");
        seller.setAccount(acc);
        seller.setAccountStatus(AccountStatus.PENDING_VERIFICATION);
        seller = sellerRepository.save(seller);

        Seller updated = sellerService.updateSellerAccountStatus(seller.getId(), AccountStatus.ACTIVE,true);
        assertEquals(AccountStatus.ACTIVE, updated.getAccountStatus());
    }
}
