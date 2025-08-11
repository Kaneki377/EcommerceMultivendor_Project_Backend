package com.zosh.model;

import com.zosh.domain.HomeCategorySection;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Table(
        name = "home_category",
        uniqueConstraints = @UniqueConstraint(columnNames = {"section","category_id"})
)
public class HomeCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String image;

    private String categoryId;

    private HomeCategorySection section;
}
