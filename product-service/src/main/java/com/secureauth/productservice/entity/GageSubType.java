package com.secureauth.productservice.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "gage_sub_types")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GageSubType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    // Bidirectional mapping to GageTypes
    @OneToMany(mappedBy = "gageSubType", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<GageType> gageTypes = new HashSet<>();

    // Bidirectional mapping to Gages
    @OneToMany(mappedBy = "gageSubType", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<Gage> gages = new HashSet<>();
}

