package com.zosh.service.impl;

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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AffiliateCampaignServiceImpl implements AffiliateCampaignService {

    private final AffiliateCampaignRepository affiliateCampaignRepository;
    private final SellerRepository sellerRepository;
    private final ProductRepository productRepository;

    @Override
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
        campaign.setSeller(seller);
        campaign.setName(request.getName());
        campaign.setDescription(request.getDescription());
        campaign.setCommissionPercent(request.getCommissionPercent());
        campaign.setCreatedAt(LocalDateTime.now());
        campaign.setExpiredAt(request.getExpiredAt());
        campaign.setActive(false); // mặc định false khi mới tạo

        // 4. Gán campaign cho từng Product
        allProducts.forEach(product -> product.setAffiliateCampaign(campaign));

        // 5. Lưu campaign (do cascade sẽ lưu luôn products nếu đã set mappedBy)
        return affiliateCampaignRepository.save(campaign);
    }
}
