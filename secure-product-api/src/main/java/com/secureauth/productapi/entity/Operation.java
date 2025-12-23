package com.secureauth.productapi.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.Set;

@Entity
@Table(name = "operations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "users") // prevents lazy-loading during toString
public class Operation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name; // e.g., op1, op2

    private String code;
    private String description;
    private Integer estimatedTimeMin;
    private String requiredSkills;
    private Boolean isMandatory = false;
    @Column(columnDefinition = "boolean default true")
    private Boolean isActive = true;

    @ManyToMany(mappedBy = "operations", fetch = FetchType.LAZY)
    @JsonIgnore
    private Set<User> users;

    // Only use id for equals/hashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Operation)) return false;
        Operation that = (Operation) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
