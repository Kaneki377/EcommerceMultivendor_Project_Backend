package com.zosh.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Table(name = "roles")
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    //Tesst commit
    @Column(length = 50, nullable = false , unique = true)
    private String name;

    public Role(String name) {
        this.name = name;
    }

    @OneToMany(mappedBy = "role")
    @JsonIgnore
    private Set<Account> accounts = new HashSet<>();
}
