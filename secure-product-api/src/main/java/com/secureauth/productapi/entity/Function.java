package com.secureauth.productapi.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.Set;

@Entity
@Table(name = "functions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "users") // optional: prevents printing lazy collection
public class Function {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name; // e.g., f1, f2, f3

    private String code;
    private String description;
    private Integer sortOrder;
    private Boolean isCritical = false;
    @Column(columnDefinition = "boolean default true")
    private Boolean isActive = true;

    @ManyToMany(mappedBy = "functions", fetch = FetchType.LAZY)
    @JsonIgnore
    private Set<User> users;

    // Use only id and name for equals/hashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Function)) return false;
        Function that = (Function) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
