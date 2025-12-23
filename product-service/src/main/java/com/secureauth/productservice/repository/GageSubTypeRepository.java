package com.secureauth.productservice.repository;

import com.secureauth.productservice.entity.GageSubType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GageSubTypeRepository extends JpaRepository<GageSubType, Long> {
    Optional<GageSubType> findByName(String name);
    boolean existsByName(String name);
}

