package com.zosh.service.impl;

import com.zosh.config.JwtProvider;
import com.zosh.exceptions.ProductException;
import com.zosh.exceptions.SellerException;
import com.zosh.model.AffiliateCampaign;
import com.zosh.model.Product;
import com.zosh.model.Seller;
import com.zosh.repository.AffiliateCampaignRepository;
import com.zosh.repository.ProductRepository;
import com.zosh.repository.SellerRepository;
import com.zosh.request.CreateAffiliateCampaignRequest;
import com.zosh.service.AffiliateCampaignService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
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

    @Override
    @Transactional
    public AffiliateCampaign createCampaign(Long sellerId, CreateAffiliateCampaignRequest request) throws SellerException,ProductException{

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
        campaign.setCampaignCode("AFF-" + seller.getId() + "-" + UUID.randomUUID().toString().substring(0, 4).toUpperCase());
        campaign.setSeller(seller);
        campaign.setName(request.getName());
        campaign.setDescription(request.getDescription());
        campaign.setCommissionPercent(request.getCommissionPercent());
        campaign.setCreatedAt(LocalDateTime.now());
       // campaign.setStartAt(request.getStartAt());
        campaign.setExpiredAt(request.getExpiredAt());
        campaign.setActive(false); // mặc định false khi mới tạo

        // 4. Gán campaign cho từng Product
        // Sau khi tạo AffiliateCampaign campaign = new AffiliateCampaign(); ... set fields ...
        AffiliateCampaign saved = affiliateCampaignRepository.save(campaign);

        // Tạo bản ghi trung gian cho từng product
        productRepository.assignCampaignToProducts(saved, request.getProductIds());

        // 5. Lưu campaign (do cascade sẽ lưu luôn products nếu đã set mappedBy)
        return saved;
    }

    @Override
    public List<AffiliateCampaign> getCampaignsBySeller(String jwt) throws SellerException {
        String username = jwtProvider.getUsernameFromJwtToken(jwt);

        Seller seller = sellerRepository.findByAccount_Username(username);
        if(seller == null) {
            throw new SellerException("Seller not found with username: " + username);
        }
        return affiliateCampaignRepository.findBySellerId(seller.getId());
    }

    @Override
    public AffiliateCampaign partialUpdate(Long campaignId, Long sellerId, Map<String, Object> updates) throws Exception {
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
        if (updates.containsKey("active")) {
            Object val = updates.get("active");
            // chấp nhận true/false kiểu boolean hoặc string
            boolean active = (val instanceof Boolean)
                    ? (Boolean) val
                    : Boolean.parseBoolean(String.valueOf(val));
            campaign.setActive(active);
        }
        if (updates.containsKey("expiredAt")) {
            // cho phép truyền ISO-8601, ví dụ "2025-12-31T23:59:59"
            campaign.setExpiredAt(LocalDateTime.parse((String) updates.get("expiredAt")));
        }

        // Nếu đang bật active mà đã quá hạn thì tự tắt, tránh "active ảo"
        if (Boolean.TRUE.equals(campaign.getActive())) {
            LocalDateTime now = LocalDateTime.now();
            if (campaign.getExpiredAt() != null && !now.isBefore(campaign.getExpiredAt())) {
                // hết hạn nếu now >= expiredAt
                campaign.setActive(false);
                // (tuỳ chọn) ném lỗi rõ ràng thay vì auto-off:
                // throw new IllegalStateException("Cannot set active because campaign is expired");
            }
        }

        return affiliateCampaignRepository.save(campaign);
    }

    @Override
    @Transactional
    public void deleteCampaign(Long campaignId, Long sellerId) throws Exception {
        AffiliateCampaign campaign = affiliateCampaignRepository.findById(campaignId)
                .orElseThrow(() -> new Exception("Campaign not found"));

        if (!campaign.getSeller().getId().equals(sellerId)) {
            throw new Exception("You do not have permission to delete this campaign");
        }
        // Cách A: xóa join trước để tránh lỗi FK
        productRepository.detachCampaignFromProducts(campaignId);

        affiliateCampaignRepository.delete(campaign);
    }

    @Override
    public boolean isActive(AffiliateCampaign campaign) {
        if (campaign == null) return false;

        // active flag phải true
        if (Boolean.FALSE.equals(campaign.getActive())) return false;

        // chưa hết hạn (expiredAt có thể null = không đặt hạn)
        var now = LocalDateTime.now();
        if (campaign.getExpiredAt() != null && now.isAfter(campaign.getExpiredAt())) {
            return false;
        }

        // (tuỳ chọn) có sản phẩm khả dụng?
        // List<Product> ps = productRepository.findByAffiliateCampaignId(campaign.getId());
        // if (ps.isEmpty()) return false;

        return true;
    }
}
