package com.secureauth.productservice.service;

import com.secureauth.productservice.dto.GageIssueDTO;
import com.secureauth.productservice.entity.GageIssue;

public interface GageIssueService {
    GageIssueDTO createIssue(GageIssueDTO dto);
    GageIssueDTO updateIssueStatus(Long issueId, GageIssue.Status status);
    GageIssueDTO updateIssueAllocation(Long issueId, String department, String functionName, String operationName, String assignedToUsername);
    java.util.List<GageIssueDTO> listIssues();
    long countIssues();
    GageIssueDTO mapToDTO(GageIssue gageIssue);
    java.util.List<GageIssueDTO> getFilteredIssues(String department, String function, String operation);
}


