package com.secureauth.productapi.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "roles")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String name; // e.g. ROLE_ADMIN or ROLE_USER

    private String description;
    @Column(columnDefinition = "boolean default true")
    private Boolean isActive = true;

    // Remove manually written constructors
    // Remove manual getters and setters (Lombok's @Data covers them)
}
