package com.zosh.service.impl;

import com.zosh.config.JwtProvider;
import com.zosh.domain.RegistrationStatus;
import com.zosh.exceptions.KocException;
import com.zosh.exceptions.SellerException;
import com.zosh.model.*;
import com.zosh.repository.*;
import com.zosh.service.AffiliateRegistrationService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AffiliateRegistrationServiceImpl implements AffiliateRegistrationService {

    private final AffiliateRegistrationRepository registrationRepository;
    private final AffiliateCampaignRepository campaignRepository;
    private final KocRepository kocRepository;
    private final SellerRepository sellerRepository;
    private final JwtProvider jwtProvider;

    @Override
    public AffiliateRegistration registerCampaign(Long campaignId, String jwt) throws KocException ,SellerException{
        String username = jwtProvider.getUsernameFromJwtToken(jwt);

        Koc koc = kocRepository.findByCustomer_Account_Username(username)
                .orElseThrow(() -> new KocException("KOC doesn't exist"));

        AffiliateCampaign campaign = campaignRepository.findById(campaignId)
                .orElseThrow(() -> new SellerException("Campaign Affiliate doesn't exist"));

        // Kiểm tra đã đăng ký chưa
        if (registrationRepository.existsByKocAndCampaign(koc, campaign)) {
            throw new RuntimeException("You have already signed up for this campaign !");
        }

        AffiliateRegistration registration = new AffiliateRegistration();
        registration.setKoc(koc);
        registration.setCampaign(campaign);
        registration.setRegisteredAt(LocalDateTime.now());
        registration.setStatus(RegistrationStatus.PENDING); // mặc định là chờ duyệt

        return registrationRepository.save(registration);
    }

    @Override
    @Transactional
    public AffiliateRegistration approveRegistration(Long registrationId, String jwt) throws SellerException{
        String username = jwtProvider.getUsernameFromJwtToken(jwt);

        Seller seller = sellerRepository.findByAccount_Username(username);
        if (seller == null) {
            throw new SellerException("Seller doesn't exist");
        }

        AffiliateRegistration registration = registrationRepository.findById(registrationId)
                .orElseThrow(() -> new RuntimeException("Registration does not exist"));

        if (!registration.getCampaign().getSeller().getId().equals(seller.getId())) {
            throw new RuntimeException("You do not have permission to approve this registration.");
        }

        registration.setStatus(RegistrationStatus.APPROVED);
        return registrationRepository.save(registration);
    }

    @Override
    @Transactional
    public AffiliateRegistration rejectRegistration(Long registrationId, String jwt) throws SellerException{
        String username = jwtProvider.getUsernameFromJwtToken(jwt);

        Seller seller = sellerRepository.findByAccount_Username(username);
        if (seller == null) {
            throw new SellerException("Seller doesn't exist");
        }

        AffiliateRegistration registration = registrationRepository.findById(registrationId)
                .orElseThrow(() -> new RuntimeException("Registration does not exist"));

        if (!registration.getCampaign().getSeller().getId().equals(seller.getId())) {
            throw new RuntimeException("You do not have permission to rejected this registration.");
        }

        registration.setStatus(RegistrationStatus.REJECTED);
        return registrationRepository.save(registration);
    }

    @Override
    public List<AffiliateRegistration> getMyRegistrations(String jwt) throws KocException{
        String username = jwtProvider.getUsernameFromJwtToken(jwt);

        Koc koc = kocRepository.findByCustomer_Account_Username(username)
                .orElseThrow(() -> new KocException("KOC doesn't exist"));

        return registrationRepository.findByKoc_Id(koc.getId());
    }

    @Override
    public List<AffiliateRegistration> getRegistrationsForMyCampaigns(String jwt) throws SellerException{
        String username = jwtProvider.getUsernameFromJwtToken(jwt);

        Seller seller = sellerRepository.findByAccount_Username(username);
        if (seller == null) {
            throw new SellerException("Seller doesn't exist");
        }

        return registrationRepository.findByCampaign_Seller_Id(seller.getId());
    }
}
