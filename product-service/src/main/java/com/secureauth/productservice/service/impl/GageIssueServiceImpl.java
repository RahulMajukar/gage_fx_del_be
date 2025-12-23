package com.secureauth.productservice.service.impl;

import com.secureauth.productservice.dto.GageIssueDTO;
import com.secureauth.productservice.entity.Gage;
import com.secureauth.productservice.entity.GageIssue;
import com.secureauth.productservice.repository.GageIssueRepository;
import com.secureauth.productservice.repository.GageRepository;
import com.secureauth.productservice.service.GageIssueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class GageIssueServiceImpl implements GageIssueService {

    @Autowired private GageIssueRepository gageIssueRepository;
    @Autowired private GageRepository gageRepository;

    @Override
    public GageIssueDTO createIssue(GageIssueDTO dto) {
        GageIssue issue = new GageIssue();
        issue.setTitle(dto.getTitle());
        issue.setDescription(dto.getDescription());
        // Priority is derived from gage.criticality; do not persist separately
        issue.setStatus(dto.getStatus() != null ? dto.getStatus() : GageIssue.Status.OPEN);
        issue.setStoreName(dto.getStore());
        issue.setAssignedTo(dto.getAssignedTo());
        issue.setAttachments(dto.getAttachments());
        issue.setTags(dto.getTags());
        issue.setSerialNumber(dto.getSerialNumber());
        issue.setDepartment(dto.getDept());
        issue.setFunctionName(dto.getFunc());
        issue.setOperationName(dto.getOperation());

        if (dto.getSerialNumber() != null && !dto.getSerialNumber().isEmpty()) {
            gageRepository.findBySerialNumber(dto.getSerialNumber()).ifPresent(gage -> {
                issue.setGage(gage);
                // priority derived via entity getter; nothing to set here
                
                // Update gage status from ACTIVE to ISSUED when issue is created
                if (gage.getStatus() == Gage.Status.ACTIVE) {
                    System.out.println("🔄 Creating issue for gage: " + dto.getSerialNumber() + " - Changing status from ACTIVE to ISSUED");
                    gage.setStatus(Gage.Status.ISSUED);
                    gageRepository.save(gage);
                    System.out.println("✅ Gage status updated to ISSUED successfully");
                } else if (gage.getStatus() == Gage.Status.ISSUED) {
                    System.out.println("ℹ️ Gage " + dto.getSerialNumber() + " is already ISSUED - no status change needed");
                } else {
                    System.out.println("⚠️ Gage " + dto.getSerialNumber() + " has status " + gage.getStatus() + " - cannot issue");
                }
            });
        }

        // No priority persistence; DTO will expose derived value

        GageIssue saved = gageIssueRepository.save(issue);
        return mapToDTO(saved);
    }

    @Override
    public java.util.List<GageIssueDTO> listIssues() {
        return gageIssueRepository.findAll().stream().map(this::mapToDTO).toList();
    }

    @Override
    public GageIssueDTO updateIssueStatus(Long issueId, GageIssue.Status status) {
        GageIssue issue = gageIssueRepository.findById(issueId)
                .orElseThrow(() -> new RuntimeException("Issue not found with id: " + issueId));
        
        issue.setStatus(status);
        GageIssue saved = gageIssueRepository.save(issue);
        
        // If issue is resolved/closed and gage is ISSUED, change gage status back to ACTIVE
        if ((status == GageIssue.Status.RESOLVED || status == GageIssue.Status.CLOSED) 
            && saved.getGage() != null && saved.getGage().getStatus() == Gage.Status.ISSUED) {
            
            Gage gage = saved.getGage();
            System.out.println("🔄 Issue resolved for gage: " + gage.getSerialNumber() + " - Changing status from ISSUED to ACTIVE");
            gage.setStatus(Gage.Status.ACTIVE);
            gageRepository.save(gage);
            System.out.println("✅ Gage status updated to ACTIVE successfully");
        }
        
        return mapToDTO(saved);
    }

    @Override
    public GageIssueDTO updateIssueAllocation(Long issueId, String department, String functionName, String operationName, String assignedToUsername) {
        GageIssue issue = gageIssueRepository.findById(issueId)
                .orElseThrow(() -> new RuntimeException("Issue not found with id: " + issueId));

        if (department != null) {
            issue.setDepartment(department);
        }
        if (functionName != null) {
            issue.setFunctionName(functionName);
        }
        if (operationName != null) {
            issue.setOperationName(operationName);
        }
        if (assignedToUsername != null) {
            issue.setAssignedTo(assignedToUsername);
        }

        GageIssue saved = gageIssueRepository.save(issue);
        return mapToDTO(saved);
    }

    @Override
    public long countIssues() {
        return gageIssueRepository.count();
    }
    
    public GageIssueDTO mapToDTO(GageIssue saved) {
        GageIssueDTO out = new GageIssueDTO();
        out.setId(saved.getId());
        out.setTitle(saved.getTitle());
        out.setDescription(saved.getDescription());
        out.setPriority(saved.getPriority()); // derived transiently from gage.criticality
        out.setStatus(saved.getStatus());
        out.setStore(saved.getStoreName());
        out.setAssignedTo(saved.getAssignedTo());
        out.setAttachments(saved.getAttachments());
        out.setTags(saved.getTags());
        out.setSerialNumber(saved.getSerialNumber());
        out.setDept(saved.getDepartment());
        out.setFunc(saved.getFunctionName());
        out.setOperation(saved.getOperationName());
        out.setCreatedAt(saved.getCreatedAt());
        return out;
    }

    @Override
    public java.util.List<GageIssueDTO> getFilteredIssues(String department, String function, String operation) {
        return gageIssueRepository.findAll().stream()
                .filter(issue -> {
                    boolean matches = true;
                    if (department != null && !department.trim().isEmpty()) {
                        matches = matches && department.equals(issue.getDepartment());
                    }
                    if (function != null && !function.trim().isEmpty()) {
                        matches = matches && function.equals(issue.getFunctionName());
                    }
                    if (operation != null && !operation.trim().isEmpty()) {
                        matches = matches && operation.equals(issue.getOperationName());
                    }
                    return matches;
                })
                .map(this::mapToDTO)
                .collect(java.util.stream.Collectors.toList());
    }

    // Mapping no longer needed; priority is derived in entity
}


