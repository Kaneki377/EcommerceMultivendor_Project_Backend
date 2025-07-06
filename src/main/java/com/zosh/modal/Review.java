package com.zosh.modal;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false)
    private String reviewText;

    @Column(nullable = false)
    private double rating;

    @ElementCollection
    private List<String> productImages;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "productId", nullable = false)
    private Product product;

    @ManyToOne
    @JoinColumn(name = "userId", nullable = false)
    private Customer customer;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}

