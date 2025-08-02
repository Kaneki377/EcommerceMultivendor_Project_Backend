package com.zosh.repository;

import com.zosh.model.Koc;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface KocRepository extends JpaRepository<Koc, Long> {
    // Tìm KOC theo customerId
    Optional<Koc> findByCustomerId(Long customerId);

    @Query("SELECT k FROM Koc k WHERE " +
            "LOWER(k.facebookLink) = LOWER(:link) OR " +
            "LOWER(k.instagramLink) = LOWER(:link) OR " +
            "LOWER(k.tiktokLink) = LOWER(:link) OR " +
            "LOWER(k.youtubeLink) = LOWER(:link)")
    Optional<Koc> findByAnySocialLink(@Param("link") String link);

    // Tìm tất cả KOC theo thời gian tham gia
    List<Koc> findByJoinedAtAfter(LocalDateTime joinedAt);

    Optional<Koc> findByKocId(String kocId);
}