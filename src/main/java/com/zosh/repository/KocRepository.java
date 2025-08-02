package com.zosh.repository;

import com.zosh.model.Koc;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface KocRepository extends JpaRepository<Koc, Long> {
    // Tìm KOC theo customerId
    Optional<Koc> findByCustomerId(Long customerId);

    // Tìm KOC theo socialLink
    Optional<Koc> findBySocialLink(String socialLink);

    // Tìm tất cả KOC theo thời gian tham gia
    List<Koc> findByJoinedAtAfter(LocalDateTime joinedAt);

    Optional<Koc> findByKocId(String kocId);
}