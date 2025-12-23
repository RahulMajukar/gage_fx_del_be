package com.secureauth.productservice.service;

import com.secureauth.productservice.dto.ReallocateApprovalRequest;
import com.secureauth.productservice.dto.ReallocateRequest;
import com.secureauth.productservice.dto.ReallocateResponse;
import com.secureauth.productservice.entity.Reallocate;

import java.util.List;

public interface ReallocateService {

    // CRUD Operations
    ReallocateResponse createReallocateRequest(ReallocateRequest request);
    ReallocateResponse getReallocateById(Long id);
    List<ReallocateResponse> getAllReallocates();
    ReallocateResponse updateReallocate(Long id, ReallocateRequest request);
    void deleteReallocate(Long id);

    // Approval Operations
    ReallocateResponse approveReallocateRequest(ReallocateApprovalRequest approvalRequest);
    ReallocateResponse rejectReallocateRequest(Long reallocateId, String rejectedBy, String reason);
    ReallocateResponse cancelReallocateRequest(Long reallocateId, String cancelledBy, String reason);

    // Return Operations
    ReallocateResponse returnGage(Long reallocateId, String returnedBy, String reason);
    ReallocateResponse forceReturnGage(Long reallocateId, String returnedBy, String reason);

    // Query Operations
    List<ReallocateResponse> getReallocatesByStatus(Reallocate.Status status);
    List<ReallocateResponse> getReallocatesByRequester(String requestedBy);
    List<ReallocateResponse> getReallocatesByApprover(String approvedBy);
    List<ReallocateResponse> getReallocatesByDepartment(String department);
    List<ReallocateResponse> getReallocatesByFunction(String function);
    List<ReallocateResponse> getReallocatesByOperation(String operation);
    List<ReallocateResponse> getReallocatesByGageId(Long gageId);
    List<ReallocateResponse> getReallocatesByGageSerialNumber(String serialNumber);

    // Filter Operations
    List<ReallocateResponse> getFilteredReallocates(String department, String function, String operation, Reallocate.Status status);
    List<ReallocateResponse> getUserInvolvedReallocates(String username);

    // Time-based Operations
    List<ReallocateResponse> getExpiredReallocates();
    List<ReallocateResponse> getReallocatesExpiringSoon();
    ReallocateResponse processExpiredReallocation(Long reallocateId);

    // Statistics
    Long countActiveReallocationsByDepartment(String department);
    Long countPendingApprovals();
    List<ReallocateResponse> getReallocatesByTimeLimit(Reallocate.TimeLimit timeLimit);

    // Validation
    boolean isGageAvailableForReallocation(Long gageId);
    boolean canUserRequestReallocation(String username, Long gageId);

    // Auto-return Operations
    void processAllExpiredReallocations();
    void sendExpirationNotifications();

    // History and Repeated Request Operations
    List<ReallocateResponse> getCompletedReallocationsForGage(Long gageId);
    List<ReallocateResponse> getReallocationHistoryForOperator(String operatorUsername);
}
