package com.zosh.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "koc")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Koc {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Giả định bạn có entity User đã định nghĩa trước
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer_id;

    @Column(name = "social_link")
    private String socialLink;

    @Column(name = "joined_at")
    private LocalDateTime joinedAt;
}
