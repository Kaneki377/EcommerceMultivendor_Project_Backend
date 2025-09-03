package com.zosh.service.impl;

import com.zosh.config.JwtProvider;
import com.zosh.exceptions.ProductException;
import com.zosh.exceptions.SellerException;
import com.zosh.model.AffiliateCampaign;
import com.zosh.model.AffiliateRegistration;
import com.zosh.model.Product;
import com.zosh.model.Seller;
import com.zosh.repository.AffiliateCampaignRepository;
import com.zosh.repository.AffiliateCommissionRepository;
import com.zosh.repository.AffiliateLinkRepository;
import com.zosh.repository.AffiliateRegistrationRepository;
import com.zosh.repository.ProductRepository;
import com.zosh.repository.SellerRepository;
import com.zosh.request.CreateAffiliateCampaignRequest;
import com.zosh.service.AffiliateCampaignService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AffiliateCampaignServiceImpl implements AffiliateCampaignService {

    private final AffiliateCampaignRepository affiliateCampaignRepository;
    private final SellerRepository sellerRepository;
    private final ProductRepository productRepository;
    private final JwtProvider jwtProvider;
    private final AffiliateCommissionRepository commissionRepository;
    private final AffiliateLinkRepository linkRepository;
    private final AffiliateRegistrationRepository registrationRepository;

    @Override
    public AffiliateCampaign createCampaign(Long sellerId, CreateAffiliateCampaignRequest request)
            throws SellerException, ProductException {

        // 1. Tìm Seller
        Seller seller = sellerRepository.findById(sellerId)
                .orElseThrow(() -> new SellerException("Seller not found with id: " + sellerId));

        // Bước 2: Lấy tất cả product theo ID bất kể seller
        List<Product> allProducts = productRepository.findAllById(request.getProductIds());

        if (allProducts.size() != request.getProductIds().size()) {
            throw new ProductException("Product doesn't exist!!");
        }

        // Kiểm tra quyền sở hữu
        List<Long> invalidProducts = allProducts.stream()
                .filter(p -> !p.getSeller().getId().equals(seller.getId()))
                .map(Product::getId)
                .toList();

        if (!invalidProducts.isEmpty()) {
            throw new SellerException("These products are from other sellers.  " + invalidProducts);
        }

        // 3. Tạo AffiliateCampaign
        AffiliateCampaign campaign = new AffiliateCampaign();
        campaign.setCampaignCode(
                "AFF-" + seller.getId() + "-" + UUID.randomUUID().toString().substring(0, 4).toUpperCase());
        campaign.setSeller(seller);
        campaign.setName(request.getName());
        campaign.setDescription(request.getDescription());
        campaign.setCommissionPercent(request.getCommissionPercent());
        campaign.setCreatedAt(LocalDateTime.now());
        campaign.setExpiredAt(request.getExpiredAt());
        campaign.setActive(false); // mặc định false khi mới tạo

        // Lưu campaign trước
        AffiliateCampaign savedCampaign = affiliateCampaignRepository.save(campaign);

        // Gán campaignId cho products
        allProducts.forEach(product -> product.setAffiliateCampaign(savedCampaign));
        productRepository.saveAll(allProducts);

        // Thêm products vào campaign để trả về đầy đủ
        savedCampaign.setProducts(allProducts);

        return savedCampaign;
    }

    @Override
    public List<AffiliateCampaign> getCampaignsBySeller(String jwt) throws SellerException {
        String username = jwtProvider.getUsernameFromJwtToken(jwt);

        Seller seller = sellerRepository.findByAccount_Username(username);
        if (seller == null) {
            throw new SellerException("Seller not found with username: " + username);
        }
        return affiliateCampaignRepository.findBySellerId(seller.getId());
    }

    @Override
    public List<AffiliateCampaign> getActiveCampaigns() {
        // Có thể lọc thêm theo expiredAt > now nếu cần
        return affiliateCampaignRepository.findByActiveTrue();
    }

    @Override
    public AffiliateCampaign partialUpdate(Long campaignId, Long sellerId, Map<String, Object> updates)
            throws Exception {
        AffiliateCampaign campaign = affiliateCampaignRepository.findById(campaignId)
                .orElseThrow(() -> new RuntimeException("Campaign not found"));

        if (!campaign.getSeller().getId().equals(sellerId)) {
            throw new RuntimeException("You do not have permission to update this campaign");
        }

        if (updates.containsKey("name")) {
            campaign.setName((String) updates.get("name"));
        }
        if (updates.containsKey("description")) {
            campaign.setDescription((String) updates.get("description"));
        }
        if (updates.containsKey("commissionPercent")) {
            campaign.setCommissionPercent(Double.parseDouble(updates.get("commissionPercent").toString()));
        }
        if (updates.containsKey("expiredAt")) {
            String raw = (String) updates.get("expiredAt");
            if (raw.length() == 10) { // yyyy-MM-dd
                LocalDate date = LocalDate.parse(raw);
                campaign.setExpiredAt(date.atTime(23, 59, 59));
            } else {
                campaign.setExpiredAt(LocalDateTime.parse(raw));
            }
        }

        if (updates.containsKey("active")) {
            boolean active = Boolean.parseBoolean(updates.get("active").toString());
            campaign.setActive(active);

            // Nếu vừa bật active thì cập nhật ngày startAt
            if (active && campaign.getStartAt() == null) {
                campaign.setStartAt(LocalDateTime.now());
            }
        }
        return affiliateCampaignRepository.save(campaign);
    }
    @ResponseStatus(HttpStatus.CONFLICT)
    class CampaignActiveException extends RuntimeException {
        CampaignActiveException(String m){ super(m); }
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    class CampaignHasRegsException extends RuntimeException {
        CampaignHasRegsException(String m){ super(m); }
    }

    @Override
    @Transactional
    public void deleteCampaign(Long campaignId, Long sellerId) throws Exception {
        var c = affiliateCampaignRepository.findById(campaignId)
                .orElseThrow(() -> new RuntimeException("Campaign not found"));

        if (!c.getSeller().getId().equals(sellerId)) {
            throw new RuntimeException("No permission");
        }
        // ❗ KIỂM TRA TRƯỚC – KHÔNG XÓA GÌ HẾT
        if (Boolean.TRUE.equals(c.getActive())) {
            throw new CampaignActiveException("Campaign has been activated");
        }
        var regs = registrationRepository.findByCampaign_Id(campaignId);
        if (!regs.isEmpty()) {
            throw new CampaignHasRegsException("Cannot delete campaign that has KOC registrations ("+regs.size()+")");
        }

        // OK thì mới gỡ liên kết & xoá
        productRepository.unlinkCampaignFromProducts(campaignId);
        linkRepository.deleteByCampaignId(campaignId);
        commissionRepository.deleteByCampaignId(campaignId);
        affiliateCampaignRepository.deleteById(campaignId);
    }

    @Override
    public List<Product> getCampaignProducts(Long campaignId, String jwt) throws Exception {
        String username = jwtProvider.getUsernameFromJwtToken(jwt);

        Seller seller = sellerRepository.findByAccount_Username(username);
        if (seller == null) {
            throw new SellerException("Seller not found with username: " + username);
        }

        AffiliateCampaign campaign = affiliateCampaignRepository.findById(campaignId)
                .orElseThrow(() -> new Exception("Campaign not found"));

        // Kiểm tra quyền sở hữu campaign
        if (!campaign.getSeller().getId().equals(seller.getId())) {
            throw new Exception("You do not have permission to view products of this campaign");
        }

        return campaign.getProducts();
    }
}
