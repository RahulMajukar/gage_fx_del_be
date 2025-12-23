package com.secureauth.productservice.controller;

import com.secureauth.productservice.dto.ReallocateApprovalRequest;
import com.secureauth.productservice.dto.ReallocateRequest;
import com.secureauth.productservice.dto.ReallocateResponse;
import com.secureauth.productservice.entity.Reallocate;
import com.secureauth.productservice.service.ReallocateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reallocates")
@RequiredArgsConstructor
@Slf4j
public class ReallocateController {

    private final ReallocateService reallocateService;

    // ================================================================
    // 🔹 CRUD Operations
    // ================================================================

    @PostMapping
    public ResponseEntity<ReallocateResponse> createReallocateRequest(@Valid @RequestBody ReallocateRequest request) {
        try {
            log.info("Creating reallocate request for gage ID: {}", request.getGageId());
            ReallocateResponse response = reallocateService.createReallocateRequest(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            log.error("Error creating reallocate request: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReallocateResponse> getReallocateById(@PathVariable Long id) {
        try {
            ReallocateResponse response = reallocateService.getReallocateById(id);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error getting reallocate by ID {}: {}", id, e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping
    public ResponseEntity<List<ReallocateResponse>> getAllReallocates() {
        try {
            List<ReallocateResponse> responses = reallocateService.getAllReallocates();
            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            log.error("Error getting all reallocates: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ReallocateResponse> updateReallocate(@PathVariable Long id, @Valid @RequestBody ReallocateRequest request) {
        try {
            ReallocateResponse response = reallocateService.updateReallocate(id, request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error updating reallocate ID {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReallocate(@PathVariable Long id) {
        try {
            reallocateService.deleteReallocate(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("Error deleting reallocate ID {}: {}", id, e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    // ================================================================
    // 🔹 Approval Operations
    // ================================================================

    @PostMapping("/approve")
    public ResponseEntity<ReallocateResponse> approveReallocateRequest(@Valid @RequestBody ReallocateApprovalRequest approvalRequest) {
        try {
            log.info("Approving reallocate request ID: {}", approvalRequest.getReallocateId());
            ReallocateResponse response = reallocateService.approveReallocateRequest(approvalRequest);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error approving reallocate request: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<ReallocateResponse> rejectReallocateRequest(
            @PathVariable Long id,
            @RequestParam String rejectedBy,
            @RequestParam(required = false) String reason) {
        try {
            log.info("Rejecting reallocate request ID: {}", id);
            ReallocateResponse response = reallocateService.rejectReallocateRequest(id, rejectedBy, reason);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error rejecting reallocate request ID {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<ReallocateResponse> cancelReallocateRequest(
            @PathVariable Long id,
            @RequestParam String cancelledBy,
            @RequestParam(required = false) String reason) {
        try {
            log.info("Cancelling reallocate request ID: {}", id);
            ReallocateResponse response = reallocateService.cancelReallocateRequest(id, cancelledBy, reason);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error cancelling reallocate request ID {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    // ================================================================
    // 🔹 Return Operations
    // ================================================================

    @PostMapping("/{id}/return")
    public ResponseEntity<ReallocateResponse> returnGage(
            @PathVariable Long id,
            @RequestParam String returnedBy,
            @RequestParam(required = false) String reason) {
        try {
            log.info("Returning gage for reallocate ID: {}", id);
            ReallocateResponse response = reallocateService.returnGage(id, returnedBy, reason);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error returning gage for reallocate ID {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/{id}/force-return")
    public ResponseEntity<ReallocateResponse> forceReturnGage(
            @PathVariable Long id,
            @RequestParam String returnedBy,
            @RequestParam(required = false) String reason) {
        try {
            log.info("Force returning gage for reallocate ID: {}", id);
            ReallocateResponse response = reallocateService.forceReturnGage(id, returnedBy, reason);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error force returning gage for reallocate ID {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    // ================================================================
    // 🔹 Query Operations
    // ================================================================

    @GetMapping("/status/{status}")
    public ResponseEntity<List<ReallocateResponse>> getReallocatesByStatus(@PathVariable Reallocate.Status status) {
        try {
            List<ReallocateResponse> responses = reallocateService.getReallocatesByStatus(status);
            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            log.error("Error getting reallocates by status {}: {}", status, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/requester/{requestedBy}")
    public ResponseEntity<List<ReallocateResponse>> getReallocatesByRequester(@PathVariable String requestedBy) {
        try {
            List<ReallocateResponse> responses = reallocateService.getReallocatesByRequester(requestedBy);
            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            log.error("Error getting reallocates by requester {}: {}", requestedBy, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/approver/{approvedBy}")
    public ResponseEntity<List<ReallocateResponse>> getReallocatesByApprover(@PathVariable String approvedBy) {
        try {
            List<ReallocateResponse> responses = reallocateService.getReallocatesByApprover(approvedBy);
            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            log.error("Error getting reallocates by approver {}: {}", approvedBy, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/department/{department}")
    public ResponseEntity<List<ReallocateResponse>> getReallocatesByDepartment(@PathVariable String department) {
        try {
            List<ReallocateResponse> responses = reallocateService.getReallocatesByDepartment(department);
            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            log.error("Error getting reallocates by department {}: {}", department, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/function/{function}")
    public ResponseEntity<List<ReallocateResponse>> getReallocatesByFunction(@PathVariable String function) {
        try {
            List<ReallocateResponse> responses = reallocateService.getReallocatesByFunction(function);
            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            log.error("Error getting reallocates by function {}: {}", function, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/operation/{operation}")
    public ResponseEntity<List<ReallocateResponse>> getReallocatesByOperation(@PathVariable String operation) {
        try {
            List<ReallocateResponse> responses = reallocateService.getReallocatesByOperation(operation);
            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            log.error("Error getting reallocates by operation {}: {}", operation, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/gage/{gageId}")
    public ResponseEntity<List<ReallocateResponse>> getReallocatesByGageId(@PathVariable Long gageId) {
        try {
            List<ReallocateResponse> responses = reallocateService.getReallocatesByGageId(gageId);
            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            log.error("Error getting reallocates by gage ID {}: {}", gageId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/gage/serial/{serialNumber}")
    public ResponseEntity<List<ReallocateResponse>> getReallocatesByGageSerialNumber(@PathVariable String serialNumber) {
        try {
            List<ReallocateResponse> responses = reallocateService.getReallocatesByGageSerialNumber(serialNumber);
            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            log.error("Error getting reallocates by gage serial number {}: {}", serialNumber, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ================================================================
    // 🔹 Filter Operations
    // ================================================================

    @GetMapping("/filtered")
    public ResponseEntity<List<ReallocateResponse>> getFilteredReallocates(
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String function,
            @RequestParam(required = false) String operation,
            @RequestParam(required = false) Reallocate.Status status) {
        try {
            List<ReallocateResponse> responses = reallocateService.getFilteredReallocates(department, function, operation, status);
            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            log.error("Error getting filtered reallocates: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/user/{username}")
    public ResponseEntity<List<ReallocateResponse>> getUserInvolvedReallocates(@PathVariable String username) {
        try {
            List<ReallocateResponse> responses = reallocateService.getUserInvolvedReallocates(username);
            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            log.error("Error getting user involved reallocates for {}: {}", username, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ================================================================
    // 🔹 Time-based Operations
    // ================================================================

    @GetMapping("/expired")
    public ResponseEntity<List<ReallocateResponse>> getExpiredReallocates() {
        try {
            List<ReallocateResponse> responses = reallocateService.getExpiredReallocates();
            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            log.error("Error getting expired reallocates: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/expiring-soon")
    public ResponseEntity<List<ReallocateResponse>> getReallocatesExpiringSoon() {
        try {
            List<ReallocateResponse> responses = reallocateService.getReallocatesExpiringSoon();
            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            log.error("Error getting reallocates expiring soon: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/{id}/process-expired")
    public ResponseEntity<ReallocateResponse> processExpiredReallocation(@PathVariable Long id) {
        try {
            log.info("Processing expired reallocation ID: {}", id);
            ReallocateResponse response = reallocateService.processExpiredReallocation(id);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error processing expired reallocation ID {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    // ================================================================
    // 🔹 Statistics
    // ================================================================

    @GetMapping("/stats/department/{department}/active-count")
    public ResponseEntity<Long> countActiveReallocationsByDepartment(@PathVariable String department) {
        try {
            Long count = reallocateService.countActiveReallocationsByDepartment(department);
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            log.error("Error counting active reallocations by department {}: {}", department, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/stats/pending-approvals")
    public ResponseEntity<Long> countPendingApprovals() {
        try {
            Long count = reallocateService.countPendingApprovals();
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            log.error("Error counting pending approvals: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/time-limit/{timeLimit}")
    public ResponseEntity<List<ReallocateResponse>> getReallocatesByTimeLimit(@PathVariable Reallocate.TimeLimit timeLimit) {
        try {
            List<ReallocateResponse> responses = reallocateService.getReallocatesByTimeLimit(timeLimit);
            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            log.error("Error getting reallocates by time limit {}: {}", timeLimit, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ================================================================
    // 🔹 Validation
    // ================================================================

    @GetMapping("/validate/gage/{gageId}/available")
    public ResponseEntity<Boolean> isGageAvailableForReallocation(@PathVariable Long gageId) {
        try {
            boolean isAvailable = reallocateService.isGageAvailableForReallocation(gageId);
            return ResponseEntity.ok(isAvailable);
        } catch (Exception e) {
            log.error("Error validating gage availability for ID {}: {}", gageId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/validate/user/{username}/gage/{gageId}/can-request")
    public ResponseEntity<Boolean> canUserRequestReallocation(
            @PathVariable String username,
            @PathVariable Long gageId) {
        try {
            boolean canRequest = reallocateService.canUserRequestReallocation(username, gageId);
            return ResponseEntity.ok(canRequest);
        } catch (Exception e) {
            log.error("Error validating user request permission for {} and gage {}: {}", username, gageId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ================================================================
    // 🔹 Auto-return Operations
    // ================================================================

    @PostMapping("/admin/process-expired")
    public ResponseEntity<String> processAllExpiredReallocations() {
        try {
            log.info("Processing all expired reallocations");
            reallocateService.processAllExpiredReallocations();
            return ResponseEntity.ok("All expired reallocations processed successfully");
        } catch (Exception e) {
            log.error("Error processing all expired reallocations: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error processing expired reallocations: " + e.getMessage());
        }
    }

    @PostMapping("/admin/send-expiration-notifications")
    public ResponseEntity<String> sendExpirationNotifications() {
        try {
            log.info("Sending expiration notifications");
            reallocateService.sendExpirationNotifications();
            return ResponseEntity.ok("Expiration notifications sent successfully");
        } catch (Exception e) {
            log.error("Error sending expiration notifications: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error sending notifications: " + e.getMessage());
        }
    }

    // ================================================================
    // 🔹 History and Repeated Request Operations
    // ================================================================

    @GetMapping("/gage/{gageId}/completed-history")
    public ResponseEntity<List<ReallocateResponse>> getCompletedReallocationsForGage(@PathVariable Long gageId) {
        try {
            List<ReallocateResponse> responses = reallocateService.getCompletedReallocationsForGage(gageId);
            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            log.error("Error getting completed reallocations for gage ID {}: {}", gageId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/operator/{operatorUsername}/history")
    public ResponseEntity<List<ReallocateResponse>> getReallocationHistoryForOperator(@PathVariable String operatorUsername) {
        try {
            List<ReallocateResponse> responses = reallocateService.getReallocationHistoryForOperator(operatorUsername);
            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            log.error("Error getting reallocation history for operator {}: {}", operatorUsername, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ================================================================
    // 🔹 Enum Endpoints
    // ================================================================

    @GetMapping("/enums/statuses")
    public ResponseEntity<Reallocate.Status[]> getStatuses() {
        return ResponseEntity.ok(Reallocate.Status.values());
    }

    @GetMapping("/enums/time-limits")
    public ResponseEntity<Reallocate.TimeLimit[]> getTimeLimits() {
        return ResponseEntity.ok(Reallocate.TimeLimit.values());
    }
}
