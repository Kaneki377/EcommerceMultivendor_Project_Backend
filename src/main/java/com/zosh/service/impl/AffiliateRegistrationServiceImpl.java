package com.zosh.service.impl;

import com.zosh.config.JwtProvider;
import com.zosh.domain.RegistrationStatus;
import com.zosh.dto.AffiliateRegistrationResponse;
import com.zosh.dto.KocRegistrationDto;
import com.zosh.dto.RegistrationApprovalResponse;
import com.zosh.exceptions.KocException;
import com.zosh.exceptions.SellerException;
import com.zosh.model.*;
import com.zosh.repository.*;
import com.zosh.service.AffiliateRegistrationService;
import com.zosh.service.AffiliateLinkService;
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
    private final AffiliateLinkService linkService;

    /**
     * KOC đăng ký tham gia chiến dịch affiliate của seller
     * 
     * @param campaignId ID của chiến dịch
     * @param jwt        JWT token của KOC
     * @return AffiliateRegistration đã được tạo
     */
    @Override
    public AffiliateRegistration registerCampaign(Long campaignId, String jwt) throws KocException, SellerException {
        String username = jwtProvider.getUsernameFromJwtToken(jwt);

        Koc koc = kocRepository.findByCustomer_Account_Username(username)
                .orElseThrow(() -> new KocException("KOC doesn't exist"));

        AffiliateCampaign campaign = campaignRepository.findById(campaignId)
                .orElseThrow(() -> new SellerException("Campaign Affiliate doesn't exist"));

        // Kiểm tra đã đăng ký chưa
        if (registrationRepository.existsByKocAndCampaign(koc, campaign)) {
            throw new KocException("You have already signed up for this campaign !");
        }

        AffiliateRegistration registration = new AffiliateRegistration();
        registration.setKoc(koc);
        registration.setCampaign(campaign);
        registration.setRegisteredAt(LocalDateTime.now());
        registration.setStatus(RegistrationStatus.PENDING); // mặc định là chờ duyệt

        return registrationRepository.save(registration);
    }

    /**
     * Seller duyệt đơn đăng ký của KOC
     * 
     * @param registrationId ID của đơn đăng ký
     * @param jwt            JWT token của seller
     * @return RegistrationApprovalResponse thông tin duyệt
     */
    @Override
    @Transactional
    public RegistrationApprovalResponse approveRegistration(Long registrationId, String jwt) throws SellerException {
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
        registrationRepository.save(registration);

        // Tự động tạo affiliate link cho KOC khi được approve
        try {
            // Tạo campaign link
//            linkService.createCampaignLink(registration.getKoc(), registration.getCampaign());

            // Tạo product links cho tất cả sản phẩm trong campaign
            for (Product product : registration.getCampaign().getProducts()) {
                try {
                    linkService.createProductLink(registration.getKoc(), product, registration.getCampaign());
                } catch (Exception productLinkError) {
                    System.err.println("Failed to create product link for product " + product.getId() + ": "
                            + productLinkError.getMessage());
                }
            }
        } catch (Exception e) {
            // Log error nhưng không fail transaction
            System.err.println("Failed to create affiliate link: " + e.getMessage());
        }

        return new RegistrationApprovalResponse(
                registration.getId(),
                registration.getCampaign().getCampaignCode(),
                registration.getCampaign().getName(),
                registration.getKoc().getKocCode(),
                registration.getStatus());
    }

    /**
     * Seller từ chối đơn đăng ký của KOC
     * 
     * @param registrationId ID của đơn đăng ký
     * @param jwt            JWT token của seller
     * @return RegistrationApprovalResponse thông tin từ chối
     */
    @Override
    @Transactional
    public RegistrationApprovalResponse rejectRegistration(Long registrationId, String jwt) throws SellerException {
        String username = jwtProvider.getUsernameFromJwtToken(jwt);

        Seller seller = sellerRepository.findByAccount_Username(username);
        if (seller == null) {
            throw new SellerException("Seller doesn't exist");
        }

        AffiliateRegistration registration = registrationRepository.findById(registrationId)
                .orElseThrow(() -> new RuntimeException("Registration does not exist"));

        if (!registration.getCampaign().getSeller().getId().equals(seller.getId())) {
            throw new RuntimeException("You do not have permission to reject this registration.");
        }

        registration.setStatus(RegistrationStatus.REJECTED);
        registrationRepository.save(registration);

        return new RegistrationApprovalResponse(
                registration.getId(),
                registration.getCampaign().getCampaignCode(),
                registration.getCampaign().getName(),
                registration.getKoc().getKocCode(),
                registration.getStatus());
    }

    /**
     * KOC xem danh sách các chiến dịch đã đăng ký
     * 
     * @param jwt JWT token của KOC
     * @return List<AffiliateRegistrationResponse> danh sách đăng ký
     */
    @Override
    public List<AffiliateRegistrationResponse> getMyRegistrations(String jwt) throws KocException {
        String username = jwtProvider.getUsernameFromJwtToken(jwt);

        Koc koc = kocRepository.findByCustomer_Account_Username(username)
                .orElseThrow(() -> new KocException("KOC doesn't exist"));

        List<AffiliateRegistration> registrations = registrationRepository.findByKoc_Id(koc.getId());

        return registrations.stream()
                .map(r -> {
                    var c = r.getCampaign();
                    var dto = new AffiliateRegistrationResponse(
                            r.getId(),
                            c.getId(),
                            c.getName(),
                            r.getRegisteredAt(),
                            r.getStatus());
                    dto.setCampaignDescription(c.getDescription());
                    dto.setCommissionPercent(c.getCommissionPercent());
                    dto.setStartedAt(c.getCreatedAt());
                    dto.setExpiredAt(c.getExpiredAt());
                    return dto;
                })
                .toList();
    }

    /**
     * Seller xem danh sách KOC đã đăng ký các chiến dịch của mình
     * 
     * @param jwt JWT token của seller
     * @return List<KocRegistrationDto> danh sách đăng ký với thông tin KOC và chiến
     *         dịch
     */
    @Override
    public List<KocRegistrationDto> getRegistrationsForMyCampaigns(String jwt) throws SellerException {
        String username = jwtProvider.getUsernameFromJwtToken(jwt);

        Seller seller = sellerRepository.findByAccount_Username(username);
        if (seller == null) {
            throw new SellerException("Seller doesn't exist");
        }

        List<AffiliateRegistration> registrations = registrationRepository.findByCampaign_Seller_Id(seller.getId());

        return registrations.stream()
                .map(r -> {
                    KocRegistrationDto dto = new KocRegistrationDto();
                    dto.setId(r.getId());
                    dto.setRegisteredAt(r.getRegisteredAt());
                    dto.setStatus(r.getStatus());

                    // Set KOC info
                    KocRegistrationDto.KocInfo kocInfo = new KocRegistrationDto.KocInfo();
                    kocInfo.setId(r.getKoc().getId());
                    kocInfo.setKocCode(r.getKoc().getKocCode());

                    KocRegistrationDto.CustomerInfo customerInfo = new KocRegistrationDto.CustomerInfo();
                    customerInfo.setFullName(r.getKoc().getCustomer().getFullName());

                    KocRegistrationDto.AccountInfo accountInfo = new KocRegistrationDto.AccountInfo();
                    accountInfo.setUsername(r.getKoc().getCustomer().getAccount().getUsername());

                    customerInfo.setAccount(accountInfo);
                    kocInfo.setCustomer(customerInfo);
                    dto.setKoc(kocInfo);

                    // Set Campaign info
                    KocRegistrationDto.CampaignInfo campaignInfo = new KocRegistrationDto.CampaignInfo();
                    campaignInfo.setId(r.getCampaign().getId());
                    campaignInfo.setCampaignCode(r.getCampaign().getCampaignCode());
                    campaignInfo.setName(r.getCampaign().getName());
                    campaignInfo.setCommissionPercent(r.getCampaign().getCommissionPercent());
                    campaignInfo.setProductCount(r.getCampaign().getProducts().size()); // Số lượng sản phẩm thật
                    dto.setCampaign(campaignInfo);

                    return dto;
                })
                .toList();
    }

}
