package com.secureauth.productservice.controller;

import com.secureauth.productservice.dto.GageIssueDTO;
import com.secureauth.productservice.entity.GageIssue;
import com.secureauth.productservice.service.GageIssueService;
import com.secureauth.productservice.service.GageFilteringService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Set;
import java.util.List;

@RestController
@RequestMapping("/api/gage-issues")
public class GageIssueController {

    @Autowired private GageIssueService gageIssueService;
    @Autowired private GageFilteringService gageFilteringService;

    @PostMapping
    public ResponseEntity<GageIssueDTO> create(@Valid @RequestBody GageIssueDTO dto) {
        // Normalize optional fields to avoid validation/parsing surprises
        if (dto.getAttachments() == null) dto.setAttachments(java.util.Collections.emptyList());
        if (dto.getTags() == null) dto.setTags(java.util.Collections.emptyList());
        GageIssueDTO saved = gageIssueService.createIssue(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @GetMapping
    public ResponseEntity<java.util.List<GageIssueDTO>> list() {
        return ResponseEntity.ok(gageIssueService.listIssues());
    }

    @PutMapping("/{issueId}/status")
    public ResponseEntity<GageIssueDTO> updateStatus(
            @PathVariable Long issueId,
            @RequestParam GageIssue.Status status) {
        try {
            GageIssueDTO updated = gageIssueService.updateIssueStatus(issueId, status);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{issueId}/allocation")
    public ResponseEntity<GageIssueDTO> updateAllocation(
            @PathVariable Long issueId,
            @RequestParam(required = false) String department,
            @RequestParam(required = false, name = "function") String functionName,
            @RequestParam(required = false, name = "operation") String operationName,
            @RequestParam(required = false, name = "assignedTo") String assignedToUsername) {
        try {
            GageIssueDTO updated = gageIssueService.updateIssueAllocation(issueId, department, functionName, operationName, assignedToUsername);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/count")
    public ResponseEntity<Long> count() {
        return ResponseEntity.ok(gageIssueService.countIssues());
    }

    /**
     * Get gage issues filtered by operator's department, function, and operation
     * This endpoint is specifically for operators to see only their assigned gages
     */
    @GetMapping("/operator-filtered")
    public ResponseEntity<List<GageIssueDTO>> getOperatorFilteredIssues(
            @RequestParam Set<String> departments,
            @RequestParam(required = false) Set<String> functions,
            @RequestParam(required = false) Set<String> operations) {
        
        try {
            // Use the existing filtering service to get filtered gage issues
            List<GageIssue> filteredIssues = gageFilteringService.getFilteredGagesForOperator(
                    departments, 
                    functions != null ? functions : Set.of(), 
                    operations != null ? operations : Set.of()
            );
            
            // Convert to DTOs
            List<GageIssueDTO> filteredDTOs = filteredIssues.stream()
                    .map(gageIssueService::mapToDTO)
                    .toList();
            
            return ResponseEntity.ok(filteredDTOs);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get gage issues filtered by operator's department, function, and operation with priority sorting
     */
    @GetMapping("/operator-filtered/priority")
    public ResponseEntity<List<GageIssueDTO>> getOperatorFilteredIssuesByPriority(
            @RequestParam Set<String> departments,
            @RequestParam(required = false) Set<String> functions,
            @RequestParam(required = false) Set<String> operations) {
        
        try {
            // Use the existing filtering service to get filtered gage issues by priority
            List<GageIssue> filteredIssues = gageFilteringService.getFilteredGagesByPriority(
                    departments, 
                    functions != null ? functions : Set.of(), 
                    operations != null ? operations : Set.of()
            );
            
            // Convert to DTOs
            List<GageIssueDTO> filteredDTOs = filteredIssues.stream()
                    .map(gageIssueService::mapToDTO)
                    .toList();
            
            return ResponseEntity.ok(filteredDTOs);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Debug endpoint to get filtering summary for testing
     */
    @GetMapping("/operator-filtered/debug")
    public ResponseEntity<GageFilteringService.FilteringSummary> getFilteringDebug(
            @RequestParam Set<String> departments,
            @RequestParam(required = false) Set<String> functions,
            @RequestParam(required = false) Set<String> operations) {
        
        try {
            GageFilteringService.FilteringSummary summary = gageFilteringService.getFilteringSummary(
                    departments, 
                    functions != null ? functions : Set.of(), 
                    operations != null ? operations : Set.of()
            );
            
            return ResponseEntity.ok(summary);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get gage issues filtered by department, function, and operation for Plant HOD
     */
    @GetMapping("/filtered")
    public ResponseEntity<List<GageIssueDTO>> getFilteredIssues(
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String function,
            @RequestParam(required = false) String operation) {
        try {
            List<GageIssueDTO> issues = gageIssueService.getFilteredIssues(department, function, operation);
            return ResponseEntity.ok(issues);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}


