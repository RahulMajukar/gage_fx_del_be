// src/main/java/com/secureauth/productapi/repository/OperationRepository.java
package com.secureauth.productapi.repository;

import com.secureauth.productapi.entity.Operation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OperationRepository extends JpaRepository<Operation, Long> {
    Optional<Operation> findByName(String name);
}
