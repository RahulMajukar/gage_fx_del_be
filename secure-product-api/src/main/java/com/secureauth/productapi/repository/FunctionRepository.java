// src/main/java/com/secureauth/productapi/repository/FunctionRepository.java
package com.secureauth.productapi.repository;

import com.secureauth.productapi.entity.Function;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FunctionRepository extends JpaRepository<Function, Long> {
    Optional<Function> findByName(String name);
}
