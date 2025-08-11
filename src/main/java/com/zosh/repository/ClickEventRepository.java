package com.zosh.repository;

import com.zosh.model.ClickEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface ClickEventRepository extends JpaRepository<ClickEvent, Long> {

    long countByAffiliateLink_Id(Long linkId);

    long countByAffiliateLink_IdAndCreatedAtBetween(Long linkId, LocalDateTime from, LocalDateTime to);

    Page<ClickEvent> findByAffiliateLink_IdOrderByCreatedAtDesc(Long linkId, Pageable pageable);

    Page<ClickEvent> findByAffiliateLink_IdAndCreatedAtBetweenOrderByCreatedAtDesc(
            Long linkId, LocalDateTime from, LocalDateTime to, Pageable pageable);

    // Đếm unique session trong khoảng thời gian
    @Query("select count(distinct e.sessionId) from ClickEvent e " +
            "where e.affiliateLink.id = :linkId and e.createdAt between :from and :to")
    long countDistinctSessionId(Long linkId, LocalDateTime from, LocalDateTime to);

    // Tổng hợp theo ngày (dùng function('date', ...) để portable)
    @Query("select function('date', e.createdAt) as d, count(e) " +
            "from ClickEvent e " +
            "where e.affiliateLink.id = :linkId and e.createdAt between :from and :to " +
            "group by function('date', e.createdAt) " +
            "order by d")
    List<Object[]> aggregateDaily(Long linkId, LocalDateTime from, LocalDateTime to);
}
