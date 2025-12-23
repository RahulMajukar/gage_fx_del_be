package com.secureauth.productservice.repository;

import com.secureauth.productservice.entity.GageIssue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface GageIssueRepository extends JpaRepository<GageIssue, Long> {
    
    // Find usage records by serial number
    List<GageIssue> findBySerialNumber(String serialNumber);
}


