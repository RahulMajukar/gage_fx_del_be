package com.secureauth.productservice.repository;

import com.secureauth.productservice.entity.GageType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GageTypeRepository extends JpaRepository<GageType, Long> {
    Optional<GageType> findByName(String name);
    boolean existsByName(String name);
}
