package com.secureauth.productapi.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.Set;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    // ====== Nested Enums ======
    public enum Location {
        BENGALURU,
        MUMBAI,
        KOLKATA,
        CHENNAI,
        HYDERABAD,

    }

    public enum Area {
        EAST,
        WEST,
        NORTH,
        SOUTH
    }

    public enum Plant {
        PLANT_A,
        PLANT_B,
        PLANT_C
    }



    // ====== Fields ======
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(unique = true)
    private String email;

    @Column(nullable = false)
    @JsonIgnore
    private String password;

    private String firstName;
    private String lastName;
    private String countryCode;
    private String phone;

    @Column(columnDefinition = "boolean default true")
    private Boolean isActive = true;

    // New enum fields
    @Enumerated(EnumType.STRING)
    private Location location;

    @Enumerated(EnumType.STRING)
    private Area area;

    @Enumerated(EnumType.STRING)
    private Plant plant;



    // Roles
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles;

    // Departments
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_departments",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "department_id")
    )
    private Set<Department> departments;

    // Functions
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_functions",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "function_id")
    )
    private Set<Function> functions;

    // Operations
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_operations",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "operation_id")
    )
    private Set<Operation> operations;

    // Optional profile image stored as Base64 string
    @Lob
    @Column(columnDefinition = "TEXT")
    private String profileImage;
}