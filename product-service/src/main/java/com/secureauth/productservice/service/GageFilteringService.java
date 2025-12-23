package com.secureauth.productservice.service;

import com.secureauth.productservice.entity.GageIssue;
import com.secureauth.productservice.repository.GageIssueRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class GageFilteringService {

    private final GageIssueRepository gageIssueRepository;

    /**
     * Smart filtering of gages based on operator context
     * Priority: Department (mandatory) > Function > Operation
     * 
     * @param operatorDepartments Operator's departments
     * @param operatorFunctions Operator's functions
     * @param operatorOperations Operator's operations
     * @return Filtered list of gage issues
     */
    public List<GageIssue> getFilteredGagesForOperator(
            Set<String> operatorDepartments,
            Set<String> operatorFunctions,
            Set<String> operatorOperations) {

        log.info("Filtering gages for operator - Depts: {}, Functions: {}, Operations: {}", 
                operatorDepartments, operatorFunctions, operatorOperations);

        // Get all gage issues
        List<GageIssue> allGages = gageIssueRepository.findAll();
        
        // Apply smart filtering
        List<GageIssue> filteredGages = allGages.stream()
                .filter(gage -> matchesOperatorContext(gage, operatorDepartments, operatorFunctions, operatorOperations))
                .collect(Collectors.toList());

        log.info("Filtered {} gages from {} total gages for operator", filteredGages.size(), allGages.size());
        
        return filteredGages;
    }

    /**
     * Smart matching logic:
     * 1. Department MUST match (mandatory)
     * 2. Function should match if available
     * 3. Operation should match if available
     * 4. Higher priority for exact matches
     */
    private boolean matchesOperatorContext(
            GageIssue gage,
            Set<String> operatorDepartments,
            Set<String> operatorFunctions,
            Set<String> operatorOperations) {

        // Department is MANDATORY - must always match
        if (!hasDepartmentMatch(gage, operatorDepartments)) {
            return false;
        }

        // Calculate match score for prioritization
        int matchScore = calculateMatchScore(gage, operatorDepartments, operatorFunctions, operatorOperations);
        
        // Log matching details for debugging
        log.debug("Gage {} - Department: {}, Function: {}, Operation: {} - Match Score: {}", 
                gage.getId(), gage.getDepartment(), gage.getFunctionName(), gage.getOperationName(), matchScore);

        return matchScore > 0; // Must have at least department match
    }

    /**
     * Check if department matches (MANDATORY)
     */
    private boolean hasDepartmentMatch(GageIssue gage, Set<String> operatorDepartments) {
        if (gage.getDepartment() == null || gage.getDepartment().trim().isEmpty()) {
            log.debug("Gage {} has no department - skipping", gage.getId());
            return false;
        }

        boolean hasMatch = operatorDepartments.contains(gage.getDepartment());
        if (!hasMatch) {
            log.debug("Gage {} department '{}' not in operator departments {}", 
                    gage.getId(), gage.getDepartment(), operatorDepartments);
        }
        return hasMatch;
    }

    /**
     * Calculate match score for prioritization
     * Higher score = better match
     */
    private int calculateMatchScore(
            GageIssue gage,
            Set<String> operatorDepartments,
            Set<String> operatorFunctions,
            Set<String> operatorOperations) {

        int score = 0;

        // Department match (mandatory - already checked)
        if (hasDepartmentMatch(gage, operatorDepartments)) {
            score += 100; // High weight for department
        }

        // Function match (if available)
        if (gage.getFunctionName() != null && !gage.getFunctionName().trim().isEmpty()) {
            if (operatorFunctions.contains(gage.getFunctionName())) {
                score += 50; // Medium weight for function
                log.debug("Gage {} function '{}' matches operator functions", 
                        gage.getId(), gage.getFunctionName());
            } else {
                log.debug("Gage {} function '{}' does not match operator functions {}", 
                        gage.getId(), gage.getFunctionName(), operatorFunctions);
            }
        } else {
            log.debug("Gage {} has no function specified", gage.getId());
        }

        // Operation match (if available)
        if (gage.getOperationName() != null && !gage.getOperationName().trim().isEmpty()) {
            if (operatorOperations.contains(gage.getOperationName())) {
                score += 25; // Lower weight for operation
                log.debug("Gage {} operation '{}' matches operator operations", 
                        gage.getId(), gage.getOperationName());
            } else {
                log.debug("Gage {} operation '{}' does not match operator operations {}", 
                        gage.getId(), gage.getOperationName(), operatorOperations);
            }
        } else {
            log.debug("Gage {} has no operation specified", gage.getId());
        }

        return score;
    }

    /**
     * Get gages by priority (highest match score first)
     */
    public List<GageIssue> getFilteredGagesByPriority(
            Set<String> operatorDepartments,
            Set<String> operatorFunctions,
            Set<String> operatorOperations) {

        List<GageIssue> filteredGages = getFilteredGagesForOperator(
                operatorDepartments, operatorFunctions, operatorOperations);

        // Sort by match score (highest first)
        return filteredGages.stream()
                .sorted((g1, g2) -> {
                    int score1 = calculateMatchScore(g1, operatorDepartments, operatorFunctions, operatorOperations);
                    int score2 = calculateMatchScore(g2, operatorDepartments, operatorFunctions, operatorOperations);
                    return Integer.compare(score2, score1); // Descending order
                })
                .collect(Collectors.toList());
    }

    /**
     * Get gages that are exact matches (all fields match)
     */
    public List<GageIssue> getExactMatches(
            Set<String> operatorDepartments,
            Set<String> operatorFunctions,
            Set<String> operatorOperations) {

        return getFilteredGagesForOperator(operatorDepartments, operatorFunctions, operatorOperations)
                .stream()
                .filter(gage -> isExactMatch(gage, operatorDepartments, operatorFunctions, operatorOperations))
                .collect(Collectors.toList());
    }

    /**
     * Check if gage is an exact match (all available fields match)
     */
    private boolean isExactMatch(
            GageIssue gage,
            Set<String> operatorDepartments,
            Set<String> operatorFunctions,
            Set<String> operatorOperations) {

        // Department must always match
        if (!hasDepartmentMatch(gage, operatorDepartments)) {
            return false;
        }

        // Function must match if specified
        if (gage.getFunctionName() != null && !gage.getFunctionName().trim().isEmpty()) {
            if (!operatorFunctions.contains(gage.getFunctionName())) {
                return false;
            }
        }

        // Operation must match if specified
        if (gage.getOperationName() != null && !gage.getOperationName().trim().isEmpty()) {
            if (!operatorOperations.contains(gage.getOperationName())) {
                return false;
            }
        }

        return true;
    }

    /**
     * Get gages that are partial matches (some fields match, some are null)
     */
    public List<GageIssue> getPartialMatches(
            Set<String> operatorDepartments,
            Set<String> operatorFunctions,
            Set<String> operatorOperations) {

        return getFilteredGagesForOperator(operatorDepartments, operatorFunctions, operatorOperations)
                .stream()
                .filter(gage -> !isExactMatch(gage, operatorDepartments, operatorFunctions, operatorOperations))
                .collect(Collectors.toList());
    }

    /**
     * Get summary statistics for filtered gages
     */
    public FilteringSummary getFilteringSummary(
            Set<String> operatorDepartments,
            Set<String> operatorFunctions,
            Set<String> operatorOperations) {

        List<GageIssue> allGages = gageIssueRepository.findAll();
        List<GageIssue> filteredGages = getFilteredGagesForOperator(
                operatorDepartments, operatorFunctions, operatorOperations);
        List<GageIssue> exactMatches = getExactMatches(
                operatorDepartments, operatorFunctions, operatorOperations);
        List<GageIssue> partialMatches = getPartialMatches(
                operatorDepartments, operatorFunctions, operatorOperations);

        return FilteringSummary.builder()
                .totalGages(allGages.size())
                .filteredGages(filteredGages.size())
                .exactMatches(exactMatches.size())
                .partialMatches(partialMatches.size())
                .operatorDepartments(operatorDepartments)
                .operatorFunctions(operatorFunctions)
                .operatorOperations(operatorOperations)
                .build();
    }

    /**
     * Summary DTO for filtering results
     */
    public static class FilteringSummary {
        private int totalGages;
        private int filteredGages;
        private int exactMatches;
        private int partialMatches;
        private Set<String> operatorDepartments;
        private Set<String> operatorFunctions;
        private Set<String> operatorOperations;

        // Builder pattern
        public static FilteringSummaryBuilder builder() {
            return new FilteringSummaryBuilder();
        }

        // Getters and setters
        public int getTotalGages() { return totalGages; }
        public void setTotalGages(int totalGages) { this.totalGages = totalGages; }
        
        public int getFilteredGages() { return filteredGages; }
        public void setFilteredGages(int filteredGages) { this.filteredGages = filteredGages; }
        
        public int getExactMatches() { return exactMatches; }
        public void setExactMatches(int exactMatches) { this.exactMatches = exactMatches; }
        
        public int getPartialMatches() { return partialMatches; }
        public void setPartialMatches(int partialMatches) { this.partialMatches = partialMatches; }
        
        public Set<String> getOperatorDepartments() { return operatorDepartments; }
        public void setOperatorDepartments(Set<String> operatorDepartments) { this.operatorDepartments = operatorDepartments; }
        
        public Set<String> getOperatorFunctions() { return operatorFunctions; }
        public void setOperatorFunctions(Set<String> operatorFunctions) { this.operatorFunctions = operatorFunctions; }
        
        public Set<String> getOperatorOperations() { return operatorOperations; }
        public void setOperatorOperations(Set<String> operatorOperations) { this.operatorOperations = operatorOperations; }

        public static class FilteringSummaryBuilder {
            private FilteringSummary summary = new FilteringSummary();

            public FilteringSummaryBuilder totalGages(int totalGages) {
                summary.totalGages = totalGages;
                return this;
            }

            public FilteringSummaryBuilder filteredGages(int filteredGages) {
                summary.filteredGages = filteredGages;
                return this;
            }

            public FilteringSummaryBuilder exactMatches(int exactMatches) {
                summary.exactMatches = exactMatches;
                return this;
            }

            public FilteringSummaryBuilder partialMatches(int partialMatches) {
                summary.partialMatches = partialMatches;
                return this;
            }

            public FilteringSummaryBuilder operatorDepartments(Set<String> operatorDepartments) {
                summary.operatorDepartments = operatorDepartments;
                return this;
            }

            public FilteringSummaryBuilder operatorFunctions(Set<String> operatorFunctions) {
                summary.operatorFunctions = operatorFunctions;
                return this;
            }

            public FilteringSummaryBuilder operatorOperations(Set<String> operatorOperations) {
                summary.operatorOperations = operatorOperations;
                return this;
            }

            public FilteringSummary build() {
                return summary;
            }
        }
    }
}

