package com.secureauth.productservice.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "gage_types")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GageType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gage_sub_type_id", nullable = false)
    private GageSubType gageSubType;

    @Column(columnDefinition = "TEXT")
    private String description;

    // Bidirectional mapping to Gages
    @OneToMany(mappedBy = "gageType", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<Gage> gages = new HashSet<>();
}
