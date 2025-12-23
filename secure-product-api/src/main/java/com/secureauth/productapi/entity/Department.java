package com.secureauth.productapi.entity;

import com.secureauth.productapi.entity.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "departments")
public class Department {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name; // Engineering , Tooling ,Machining ,Quality

    private String type;
    private String contactPerson;
    private String contactEmail;
    private String contactPhone;
    private String costCenter;
    private Double budget;
    @Column(columnDefinition = "boolean default true")
    private Boolean isActive = true;

    @ManyToMany(mappedBy = "departments")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @JsonIgnore
    private Set<User> users;
}
