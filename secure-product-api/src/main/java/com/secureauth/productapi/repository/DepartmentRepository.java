// src/main/java/com/secureauth/productapi/repository/DepartmentRepository.java
package com.secureauth.productapi.repository;

import com.secureauth.productapi.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DepartmentRepository extends JpaRepository<Department, Long> {
    Optional<Department> findByName(String name);
}
