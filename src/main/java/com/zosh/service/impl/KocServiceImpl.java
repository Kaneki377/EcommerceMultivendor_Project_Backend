package com.zosh.service.impl;

import com.zosh.exceptions.CustomerException;
import com.zosh.exceptions.KocException;
import com.zosh.model.Account;
import com.zosh.model.Customer;
import com.zosh.model.Koc;
import com.zosh.model.Role;
import com.zosh.repository.CustomerRepository;
import com.zosh.repository.KocRepository;
import com.zosh.repository.RoleRepository;
import com.zosh.request.CreateKocRequest;
import com.zosh.service.KocService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class KocServiceImpl implements KocService {

    private final KocRepository kocRepository;

    private final CustomerRepository customerRepository;

    private final RoleRepository roleRepository;


    @Override
    public Koc createKoc(CreateKocRequest request) {
        Long customerId = request.getCustomerId();

        // Kiểm tra phải có ít nhất 1 link
        if (!request.hasAtLeastOneLink()) {
            throw new KocException("Must provide at least one social media link !");
        }

        // Tìm customer
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerException("Customer not found with ID: " + customerId));

        // Kiểm tra đã là KOC chưa
        if (customer.isKoc() || kocRepository.findByCustomerId(customerId).isPresent()) {
            throw new KocException("Customer is already a KOC");
        }

        // Tạo KOC
        Koc koc = new Koc();
        koc.setCustomer(customer);
        koc.setKocId("KOC" + UUID.randomUUID().toString().substring(0, 8));
        koc.setFacebookLink(request.getFacebookLink());
        koc.setInstagramLink(request.getInstagramLink());
        koc.setTiktokLink(request.getTiktokLink());
        koc.setYoutubeLink(request.getYoutubeLink());
        koc.setJoinedAt(LocalDateTime.now());

        // Cập nhật customer + role
        customer.setKoc(true);
        Account account = customer.getAccount();
        Role kocRole = roleRepository.findByName("ROLE_KOC");
        account.setRole(kocRole);
        account.setIsEnabled(true);
        customerRepository.save(customer);

        return kocRepository.save(koc);
    }
}