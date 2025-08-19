package com.zosh.repository;

import com.zosh.domain.RegistrationStatus;
import com.zosh.model.AffiliateCampaign;
import com.zosh.model.AffiliateRegistration;
import com.zosh.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AffiliateCampaignRepository extends JpaRepository<AffiliateCampaign, Long> {
    // Tìm campaign theo sellerId
    List<AffiliateCampaign> findBySellerId(Long sellerId);

    // Tìm campaign đang hoạt động
    List<AffiliateCampaign> findByActiveTrue();

    // Tìm campaign theo tên (gần đúng)
    List<AffiliateCampaign> findByNameContainingIgnoreCase(String name);

    // a) Tất cả campaign active, chưa hết hạn
    @Query("""
        select c
        from AffiliateCampaign c
        where c.active = true
          and (c.expiredAt is null or c.expiredAt > :now)
        """)
    Page<AffiliateCampaign> findAllActive(@Param("now") LocalDateTime now, Pageable pageable);

    // b) Tất cả campaign active, chưa hết hạn, và KOC CHƯA đăng ký
    @Query("""
        select c
        from AffiliateCampaign c
        where c.active = true
          and (c.expiredAt is null or c.expiredAt > :now)
          and not exists (
            select r.id
            from AffiliateRegistration r
            where r.campaign.id = c.id
              and r.koc.id = :kocId
          )
        """)
    Page<AffiliateCampaign> findActiveNotRegisteredByKoc(@Param("kocId") Long kocId, @Param("now") LocalDateTime now,
                                                         Pageable pageable);

    // c) Nếu bạn muốn trả kèm myStatus (LEFT JOIN) cho tất cả active campaigns
    @Query("""
        select c as campaign, r.status as myStatus
        from AffiliateCampaign c
        left join AffiliateRegistration r
          on r.campaign.id = c.id and r.koc.id = :kocId
        where c.active = true
          and (c.expiredAt is null or c.expiredAt > :now)
        """)
    Page<CampaignWithStatusProjection> findActiveWithMyStatus(@Param("kocId") Long kocId,
                                                              @Param("now") LocalDateTime now,
                                                              Pageable pageable);

    interface CampaignWithStatusProjection {
        AffiliateCampaign getCampaign();
        RegistrationStatus getMyStatus(); // sửa enum path theo project của bạn
    }

}