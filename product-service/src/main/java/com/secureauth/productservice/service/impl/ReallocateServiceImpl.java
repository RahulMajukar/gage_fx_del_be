package com.secureauth.productservice.service.impl;

import com.secureauth.productservice.dto.ReallocateApprovalRequest;
import com.secureauth.productservice.dto.ReallocateRequest;
import com.secureauth.productservice.dto.ReallocateResponse;
import com.secureauth.productservice.entity.Gage;
import com.secureauth.productservice.entity.GageIssue;
import com.secureauth.productservice.entity.Reallocate;
import com.secureauth.productservice.repository.GageIssueRepository;
import com.secureauth.productservice.repository.GageRepository;
import com.secureauth.productservice.repository.ReallocateRepository;
import com.secureauth.productservice.service.ReallocateService;
import com.secureauth.productservice.service.ReallocateNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ReallocateServiceImpl implements ReallocateService {

    private final ReallocateRepository reallocateRepository;
    private final GageRepository gageRepository;
    private final GageIssueRepository gageIssueRepository;
    
    @Autowired
    @Lazy
    private ReallocateNotificationService notificationService;

    @Override
    public ReallocateResponse createReallocateRequest(ReallocateRequest request) {
        log.info("Creating reallocate request for gage ID: {}", request.getGageId());

        // Validate gage exists and is available
        Gage gage = gageRepository.findById(request.getGageId())
                .orElseThrow(() -> new RuntimeException("Gage not found with ID: " + request.getGageId()));

        if (!isGageAvailableForReallocation(request.getGageId())) {
            throw new RuntimeException("Gage is already allocated or not available for reallocation");
        }

        // Determine original allocation details from the latest gage issue for this serial number
        String originalDepartment = "DEFAULT_DEPT";
        String originalFunction = "DEFAULT_FUNC";
        String originalOperation = "DEFAULT_OP";

        try {
            List<GageIssue> issuesForGage = gageIssueRepository.findBySerialNumber(gage.getSerialNumber());
            issuesForGage.stream()
                    .max(Comparator.comparing(GageIssue::getCreatedAt))
                    .ifPresent(lastIssue -> {
                        // Use non-null values from the latest issue when available
                        String dept = lastIssue.getDepartment();
                        String func = lastIssue.getFunctionName();
                        String op = lastIssue.getOperationName();

                        // Fallback to existing defaults if null/blank
                        if (dept != null && !dept.isBlank()) {
                            // capture effectively final via array wrapper if needed; here, assign to outer via array not necessary since we set local vars before builder
                        }
                    });
            // After peek, set from max again to capture values for builder
            issuesForGage.stream()
                    .max(Comparator.comparing(GageIssue::getCreatedAt))
                    .ifPresent(lastIssue -> {
                        // assign computed originals
                    });
            // Simple direct assignment using the lastIssue values
            if (!issuesForGage.isEmpty()) {
                GageIssue lastIssue = issuesForGage.stream().max(Comparator.comparing(GageIssue::getCreatedAt)).orElse(null);
                if (lastIssue != null) {
                    originalDepartment = lastIssue.getDepartment() != null && !lastIssue.getDepartment().isBlank() ? lastIssue.getDepartment() : originalDepartment;
                    originalFunction = lastIssue.getFunctionName() != null && !lastIssue.getFunctionName().isBlank() ? lastIssue.getFunctionName() : originalFunction;
                    originalOperation = lastIssue.getOperationName() != null && !lastIssue.getOperationName().isBlank() ? lastIssue.getOperationName() : originalOperation;
                }
            }
        } catch (Exception ex) {
            log.warn("Could not derive original allocation from GageIssue for serial {}: {}", gage.getSerialNumber(), ex.getMessage());
        }

        Reallocate reallocate = Reallocate.builder()
                .gage(gage)
                .originalDepartment(originalDepartment)
                .originalFunction(originalFunction)
                .originalOperation(originalOperation)
                .currentDepartment(originalDepartment)
                .currentFunction(originalFunction)
                .currentOperation(originalOperation)
                .requestedBy(request.getRequestedBy())
                .requestedByRole(request.getRequestedByRole())
                .requestedByFunction(request.getRequestedByFunction())
                .requestedByOperation(request.getRequestedByOperation())
                .timeLimit(request.getTimeLimit())
                .reason(request.getReason())
                .notes(request.getNotes())
                .status(Reallocate.Status.PENDING_APPROVAL)
                .build();

        Reallocate savedReallocate = reallocateRepository.save(reallocate);
        log.info("Reallocate request created with ID: {}", savedReallocate.getId());

        // Send notification to Plant HOD
        ReallocateResponse response = convertToResponse(savedReallocate);
        notificationService.sendReallocationRequestNotification(response);

        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public ReallocateResponse getReallocateById(Long id) {
        Reallocate reallocate = reallocateRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reallocate not found with ID: " + id));
        return convertToResponse(reallocate);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReallocateResponse> getAllReallocates() {
        return reallocateRepository.findAll().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public ReallocateResponse updateReallocate(Long id, ReallocateRequest request) {
        Reallocate reallocate = reallocateRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reallocate not found with ID: " + id));

        if (reallocate.getStatus() != Reallocate.Status.PENDING_APPROVAL) {
            throw new RuntimeException("Cannot update reallocate request that is not pending approval");
        }

        reallocate.setRequestedBy(request.getRequestedBy());
        reallocate.setRequestedByRole(request.getRequestedByRole());
        reallocate.setRequestedByFunction(request.getRequestedByFunction());
        reallocate.setRequestedByOperation(request.getRequestedByOperation());
        reallocate.setTimeLimit(request.getTimeLimit());
        reallocate.setReason(request.getReason());
        reallocate.setNotes(request.getNotes());

        Reallocate updatedReallocate = reallocateRepository.save(reallocate);
        return convertToResponse(updatedReallocate);
    }

    @Override
    public void deleteReallocate(Long id) {
        Reallocate reallocate = reallocateRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reallocate not found with ID: " + id));

        if (reallocate.getStatus() == Reallocate.Status.APPROVED) {
            throw new RuntimeException("Cannot delete approved reallocate request");
        }

        reallocateRepository.delete(reallocate);
    }

    @Override
    public ReallocateResponse approveReallocateRequest(ReallocateApprovalRequest approvalRequest) {
        log.info("Approving reallocate request ID: {}", approvalRequest.getReallocateId());

        Reallocate reallocate = reallocateRepository.findById(approvalRequest.getReallocateId())
                .orElseThrow(() -> new RuntimeException("Reallocate not found with ID: " + approvalRequest.getReallocateId()));

        if (reallocate.getStatus() != Reallocate.Status.PENDING_APPROVAL) {
            throw new RuntimeException("Cannot approve reallocate request that is not pending approval");
        }

        // Update allocation details if provided
        if (approvalRequest.getNewDepartment() != null) {
            reallocate.setCurrentDepartment(approvalRequest.getNewDepartment());
        }
        if (approvalRequest.getNewFunction() != null) {
            reallocate.setCurrentFunction(approvalRequest.getNewFunction());
        }
        if (approvalRequest.getNewOperation() != null) {
            reallocate.setCurrentOperation(approvalRequest.getNewOperation());
        }

        // Update time limit if provided
        if (approvalRequest.getTimeLimit() != null) {
            reallocate.setTimeLimit(approvalRequest.getTimeLimit());
        }

        reallocate.setApprovedBy(approvalRequest.getApprovedBy());
        reallocate.setApprovedAt(LocalDateTime.now());
        reallocate.setAllocatedAt(LocalDateTime.now());
        reallocate.setStatus(Reallocate.Status.APPROVED);
        reallocate.setNotes(approvalRequest.getNotes());

        // Calculate expiry time
        reallocate.calculateExpiryTime();

        Reallocate savedReallocate = reallocateRepository.save(reallocate);

        // Update gage status to ISSUED
        Gage gage = savedReallocate.getGage();
        gage.setStatus(Gage.Status.ISSUED);
        gageRepository.save(gage);

        log.info("Reallocate request approved and gage status updated");

        // Send notification to operator(s) and capture who was notified
        ReallocateResponse response = convertToResponse(savedReallocate);
        java.util.List<String> notified = notificationService.sendApprovalNotification(response, true);

        // Append notified operators to notes so frontend operator dashboards can filter notifications
        if (notified != null && !notified.isEmpty()) {
            String notifyNotes = String.join(",", notified);
            String existingNotes = savedReallocate.getNotes() == null ? "" : savedReallocate.getNotes();
            String appended = existingNotes.isBlank() ? "Notify Operator: " + notifyNotes : existingNotes + " | Notify Operator: " + notifyNotes;
            savedReallocate.setNotes(appended);
            savedReallocate = reallocateRepository.save(savedReallocate);
            response = convertToResponse(savedReallocate);
        }

        // Now apply allocation to the latest issue, and assign to the first notified operator (if any)
        applyTemporaryAllocationToLatestIssue(savedReallocate, notified);

        return response;
    }

    @Override
    public ReallocateResponse rejectReallocateRequest(Long reallocateId, String rejectedBy, String reason) {
        Reallocate reallocate = reallocateRepository.findById(reallocateId)
                .orElseThrow(() -> new RuntimeException("Reallocate not found with ID: " + reallocateId));

        if (reallocate.getStatus() != Reallocate.Status.PENDING_APPROVAL) {
            throw new RuntimeException("Cannot reject reallocate request that is not pending approval");
        }

        reallocate.setStatus(Reallocate.Status.CANCELLED);
        reallocate.setNotes(reason);
        reallocate.setApprovedBy(rejectedBy);
        reallocate.setApprovedAt(LocalDateTime.now());

        Reallocate savedReallocate = reallocateRepository.save(reallocate);
        
        // Send notification to operator(s) about rejection
        ReallocateResponse response = convertToResponse(savedReallocate);
        java.util.List<String> notified = notificationService.sendApprovalNotification(response, false);
        if (notified != null && !notified.isEmpty()) {
            String notifyNotes = String.join(",", notified);
            String existingNotes = savedReallocate.getNotes() == null ? "" : savedReallocate.getNotes();
            String appended = existingNotes.isBlank() ? "Notify Operator: " + notifyNotes : existingNotes + " | Notify Operator: " + notifyNotes;
            savedReallocate.setNotes(appended);
            savedReallocate = reallocateRepository.save(savedReallocate);
            response = convertToResponse(savedReallocate);
        }

        return response;
    }

    @Override
    public ReallocateResponse cancelReallocateRequest(Long reallocateId, String cancelledBy, String reason) {
        Reallocate reallocate = reallocateRepository.findById(reallocateId)
                .orElseThrow(() -> new RuntimeException("Reallocate not found with ID: " + reallocateId));

        if (reallocate.getStatus() == Reallocate.Status.RETURNED) {
            throw new RuntimeException("Cannot cancel already returned reallocate request");
        }

        reallocate.setStatus(Reallocate.Status.CANCELLED);
        reallocate.setNotes(reason);
        reallocate.setApprovedBy(cancelledBy);
        reallocate.setApprovedAt(LocalDateTime.now());

        Reallocate savedReallocate = reallocateRepository.save(reallocate);
        return convertToResponse(savedReallocate);
    }

    @Override
    public ReallocateResponse returnGage(Long reallocateId, String returnedBy, String reason) {
        Reallocate reallocate = reallocateRepository.findById(reallocateId)
                .orElseThrow(() -> new RuntimeException("Reallocate not found with ID: " + reallocateId));

        if (reallocate.getStatus() != Reallocate.Status.APPROVED) {
            throw new RuntimeException("Cannot return gage that is not approved");
        }

        reallocate.setStatus(Reallocate.Status.RETURNED);
        reallocate.setNotes(reason);
        reallocate.setApprovedBy(returnedBy);
        reallocate.setApprovedAt(LocalDateTime.now());

        // Update gage status back to ACTIVE
        Gage gage = reallocate.getGage();
        gage.setStatus(Gage.Status.ACTIVE);
        gageRepository.save(gage);

        // Revert gage issue allocation back to original dept/function/operation
        revertAllocationOnLatestIssue(reallocate);

        // Set status to COMPLETED to allow new requests
        reallocate.setStatus(Reallocate.Status.COMPLETED);
        Reallocate savedReallocate = reallocateRepository.save(reallocate);
        
        // Send notification about return
        ReallocateResponse response = convertToResponse(savedReallocate);
        notificationService.sendAutoReturnNotification(response);
        
        return response;
    }

    @Override
    public ReallocateResponse forceReturnGage(Long reallocateId, String returnedBy, String reason) {
        Reallocate reallocate = reallocateRepository.findById(reallocateId)
                .orElseThrow(() -> new RuntimeException("Reallocate not found with ID: " + reallocateId));

        reallocate.setStatus(Reallocate.Status.RETURNED);
        reallocate.setNotes(reason);
        reallocate.setApprovedBy(returnedBy);
        reallocate.setApprovedAt(LocalDateTime.now());

        // Update gage status back to ACTIVE
        Gage gage = reallocate.getGage();
        gage.setStatus(Gage.Status.ACTIVE);
        gageRepository.save(gage);

        Reallocate savedReallocate = reallocateRepository.save(reallocate);
        
        // Send notification about force return
        ReallocateResponse response = convertToResponse(savedReallocate);
        notificationService.sendAutoReturnNotification(response);
        
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReallocateResponse> getReallocatesByStatus(Reallocate.Status status) {
        return reallocateRepository.findByStatus(status).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReallocateResponse> getReallocatesByRequester(String requestedBy) {
        return reallocateRepository.findByRequestedBy(requestedBy).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReallocateResponse> getReallocatesByApprover(String approvedBy) {
        return reallocateRepository.findByApprovedBy(approvedBy).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReallocateResponse> getReallocatesByDepartment(String department) {
        return reallocateRepository.findByCurrentDepartment(department).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReallocateResponse> getReallocatesByFunction(String function) {
        return reallocateRepository.findByCurrentFunction(function).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReallocateResponse> getReallocatesByOperation(String operation) {
        return reallocateRepository.findByCurrentOperation(operation).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReallocateResponse> getReallocatesByGageId(Long gageId) {
        return reallocateRepository.findByGageSerialNumber(
                gageRepository.findById(gageId)
                        .orElseThrow(() -> new RuntimeException("Gage not found"))
                        .getSerialNumber()
        ).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReallocateResponse> getReallocatesByGageSerialNumber(String serialNumber) {
        return reallocateRepository.findByGageSerialNumber(serialNumber).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReallocateResponse> getFilteredReallocates(String department, String function, String operation, Reallocate.Status status) {
        return reallocateRepository.findByMultipleCriteria(department, function, operation, status).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReallocateResponse> getUserInvolvedReallocates(String username) {
        return reallocateRepository.findByUserInvolvement(username).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReallocateResponse> getExpiredReallocates() {
        return reallocateRepository.findExpiredReallocations(LocalDateTime.now()).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReallocateResponse> getReallocatesExpiringSoon() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime oneHourFromNow = now.plusHours(1);
        return reallocateRepository.findReallocationsExpiringSoon(now, oneHourFromNow).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public ReallocateResponse processExpiredReallocation(Long reallocateId) {
        Reallocate reallocate = reallocateRepository.findById(reallocateId)
                .orElseThrow(() -> new RuntimeException("Reallocate not found with ID: " + reallocateId));

        if (reallocate.getStatus() != Reallocate.Status.APPROVED) {
            throw new RuntimeException("Cannot process expired reallocation that is not approved");
        }

        if (!reallocate.isExpired()) {
            throw new RuntimeException("Reallocation is not expired yet");
        }

        // Auto-return the gage and mark as completed
        returnGage(reallocateId, "SYSTEM", "Automatically returned due to expiration");
        
        // Mark the reallocation as completed to allow new requests
        reallocate.setStatus(Reallocate.Status.COMPLETED);
        reallocateRepository.save(reallocate);
        
        return convertToResponse(reallocate);
    }

    @Override
    @Transactional(readOnly = true)
    public Long countActiveReallocationsByDepartment(String department) {
        return reallocateRepository.countActiveReallocationsByDepartment(department);
    }

    @Override
    @Transactional(readOnly = true)
    public Long countPendingApprovals() {
        return reallocateRepository.countPendingApprovals();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReallocateResponse> getReallocatesByTimeLimit(Reallocate.TimeLimit timeLimit) {
        return reallocateRepository.findByTimeLimit(timeLimit).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isGageAvailableForReallocation(Long gageId) {
        // Gage is available if there are no active reallocations (APPROVED, PENDING_APPROVAL, EXPIRED)
        // RETURNED and COMPLETED statuses allow new requests
        return reallocateRepository.countActiveReallocationsByGageId(gageId) == 0;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean canUserRequestReallocation(String username, Long gageId) {
        // Check if gage is available for reallocation
        if (!isGageAvailableForReallocation(gageId)) {
            return false;
        }
        
        // Allow repeated requests from the same operator after completion
        // This enables the cycle: Request → Approve → Use → Return → Request Again
        return true;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReallocateResponse> getCompletedReallocationsForGage(Long gageId) {
        List<Reallocate> completedReallocations = reallocateRepository.findCompletedReallocationsByGageId(gageId);
        return completedReallocations.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReallocateResponse> getReallocationHistoryForOperator(String operatorUsername) {
        List<Reallocate> operatorHistory = reallocateRepository.findByRequestedBy(operatorUsername);
        return operatorHistory.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void processAllExpiredReallocations() {
        log.info("Processing all expired reallocations");
        List<Reallocate> expiredReallocations = reallocateRepository.findExpiredReallocations(LocalDateTime.now());
        
        for (Reallocate reallocate : expiredReallocations) {
            try {
                processExpiredReallocation(reallocate.getId());
                log.info("Processed expired reallocation ID: {}", reallocate.getId());
            } catch (Exception e) {
                log.error("Error processing expired reallocation ID: {}", reallocate.getId(), e);
            }
        }
    }

    @Override
    public void sendExpirationNotifications() {
        log.info("Sending expiration notifications");
        List<Reallocate> expiringSoon = reallocateRepository.findReallocationsExpiringSoon(
                LocalDateTime.now(), LocalDateTime.now().plusHours(1));
        
        for (Reallocate reallocate : expiringSoon) {
            // TODO: Implement notification logic (email, SMS, etc.)
            log.info("Sending expiration notification for reallocation ID: {}", reallocate.getId());
        }
    }

    // ---------------------------------------------------------------------
    // Helper methods to sync temporary allocation with latest GageIssue
    // ---------------------------------------------------------------------
    private void applyTemporaryAllocationToLatestIssue(Reallocate reallocate, java.util.List<String> notifiedOperators) {
        try {
            String serial = reallocate.getGage().getSerialNumber();
            List<GageIssue> issues = gageIssueRepository.findBySerialNumber(serial);
            if (issues == null || issues.isEmpty()) {
                return;
            }
            GageIssue latest = issues.stream()
                    .max(Comparator.comparing(GageIssue::getCreatedAt))
                    .orElse(null);
            if (latest == null) {
                return;
            }
            if (reallocate.getCurrentDepartment() != null) latest.setDepartment(reallocate.getCurrentDepartment());
            if (reallocate.getCurrentFunction() != null) latest.setFunctionName(reallocate.getCurrentFunction());
            if (reallocate.getCurrentOperation() != null) latest.setOperationName(reallocate.getCurrentOperation());

            // Assign to first notified operator (if any)
            if (notifiedOperators != null && !notifiedOperators.isEmpty()) {
                String first = notifiedOperators.get(0);
                if (first != null && !first.isBlank()) {
                    latest.setAssignedTo(first);
                    latest.setOperatorUsername(first);
                }
            }

            gageIssueRepository.save(latest);
            log.info("Applied temporary allocation to latest issue for serial {} -> {}/{}/{}", serial,
                    latest.getDepartment(), latest.getFunctionName(), latest.getOperationName());
        } catch (Exception ex) {
            log.warn("Failed to apply temporary allocation to latest issue: {}", ex.getMessage());
        }
    }

    private void revertAllocationOnLatestIssue(Reallocate reallocate) {
        try {
            String serial = reallocate.getGage().getSerialNumber();
            List<GageIssue> issues = gageIssueRepository.findBySerialNumber(serial);
            if (issues == null || issues.isEmpty()) {
                return;
            }
            GageIssue latest = issues.stream()
                    .max(Comparator.comparing(GageIssue::getCreatedAt))
                    .orElse(null);
            if (latest == null) {
                return;
            }
            if (reallocate.getOriginalDepartment() != null) latest.setDepartment(reallocate.getOriginalDepartment());
            if (reallocate.getOriginalFunction() != null) latest.setFunctionName(reallocate.getOriginalFunction());
            if (reallocate.getOriginalOperation() != null) latest.setOperationName(reallocate.getOriginalOperation());
            // Clear assignment when reverting
            latest.setAssignedTo(null);
            latest.setOperatorUsername(null);
            gageIssueRepository.save(latest);
            log.info("Reverted allocation on latest issue for serial {} -> {}/{}/{}", serial,
                    latest.getDepartment(), latest.getFunctionName(), latest.getOperationName());
        } catch (Exception ex) {
            log.warn("Failed to revert allocation on latest issue: {}", ex.getMessage());
        }
    }

    private ReallocateResponse convertToResponse(Reallocate reallocate) {
        return ReallocateResponse.builder()
                .id(reallocate.getId())
                .gageId(reallocate.getGage().getId())
                .gageSerialNumber(reallocate.getGage().getSerialNumber())
                .gageModelNumber(reallocate.getGage().getModelNumber())
                .gageTypeName(reallocate.getGage().getGageType().getName())
                .originalDepartment(reallocate.getOriginalDepartment())
                .originalFunction(reallocate.getOriginalFunction())
                .originalOperation(reallocate.getOriginalOperation())
                .currentDepartment(reallocate.getCurrentDepartment())
                .currentFunction(reallocate.getCurrentFunction())
                .currentOperation(reallocate.getCurrentOperation())
                .requestedBy(reallocate.getRequestedBy())
                .requestedByRole(reallocate.getRequestedByRole())
                .requestedByFunction(reallocate.getRequestedByFunction())
                .requestedByOperation(reallocate.getRequestedByOperation())
                .approvedBy(reallocate.getApprovedBy())
                .approvedAt(reallocate.getApprovedAt())
                .timeLimit(reallocate.getTimeLimit())
                .allocatedAt(reallocate.getAllocatedAt())
                .expiresAt(reallocate.getExpiresAt())
                .status(reallocate.getStatus())
                .reason(reallocate.getReason())
                .notes(reallocate.getNotes())
                .remainingMinutes(reallocate.getRemainingMinutes())
                .isExpired(reallocate.isExpired())
                .createdAt(reallocate.getCreatedAt())
                .updatedAt(reallocate.getUpdatedAt())
                .build();
    }
}
