package com.secureauth.productservice.repository;

import com.secureauth.productservice.entity.Reallocate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReallocateRepository extends JpaRepository<Reallocate, Long> {

    // Find active reallocations for a specific gage (excluding completed and cancelled)
    @Query("SELECT r FROM Reallocate r WHERE r.gage.id = :gageId AND r.status IN ('APPROVED', 'PENDING_APPROVAL', 'EXPIRED', 'RETURNED')")
    Optional<Reallocate> findActiveReallocationByGageId(@Param("gageId") Long gageId);

    // Find reallocations by status
    List<Reallocate> findByStatus(Reallocate.Status status);

    // Find reallocations by requester
    List<Reallocate> findByRequestedBy(String requestedBy);

    // Find reallocations by approver
    List<Reallocate> findByApprovedBy(String approvedBy);

    // Find reallocations by department
    List<Reallocate> findByCurrentDepartment(String department);

    // Find reallocations by function
    List<Reallocate> findByCurrentFunction(String function);

    // Find reallocations by operation
    List<Reallocate> findByCurrentOperation(String operation);

    // Find expired reallocations that need to be returned
    @Query("SELECT r FROM Reallocate r WHERE r.status = 'APPROVED' AND r.expiresAt < :currentTime")
    List<Reallocate> findExpiredReallocations(@Param("currentTime") LocalDateTime currentTime);

    // Find reallocations that are available for new requests (completed status)
    @Query("SELECT r FROM Reallocate r WHERE r.gage.id = :gageId AND r.status = 'COMPLETED' ORDER BY r.updatedAt DESC")
    List<Reallocate> findCompletedReallocationsByGageId(@Param("gageId") Long gageId);

    // Check if gage is available for new reallocation requests
    @Query("SELECT COUNT(r) FROM Reallocate r WHERE r.gage.id = :gageId AND r.status IN ('APPROVED', 'PENDING_APPROVAL', 'EXPIRED')")
    Long countActiveReallocationsByGageId(@Param("gageId") Long gageId);

    // Find reallocations expiring soon (within next hour)
    @Query("SELECT r FROM Reallocate r WHERE r.status = 'APPROVED' AND r.expiresAt BETWEEN :currentTime AND :oneHourFromNow")
    List<Reallocate> findReallocationsExpiringSoon(@Param("currentTime") LocalDateTime currentTime, 
                                                   @Param("oneHourFromNow") LocalDateTime oneHourFromNow);

    // Find reallocations by gage serial number
    @Query("SELECT r FROM Reallocate r WHERE r.gage.serialNumber = :serialNumber")
    List<Reallocate> findByGageSerialNumber(@Param("serialNumber") String serialNumber);

    // Find reallocations by multiple criteria
    @Query("SELECT r FROM Reallocate r WHERE " +
           "(:department IS NULL OR r.currentDepartment = :department) AND " +
           "(:function IS NULL OR r.currentFunction = :function) AND " +
           "(:operation IS NULL OR r.currentOperation = :operation) AND " +
           "(:status IS NULL OR r.status = :status)")
    List<Reallocate> findByMultipleCriteria(@Param("department") String department,
                                           @Param("function") String function,
                                           @Param("operation") String operation,
                                           @Param("status") Reallocate.Status status);

    // Find reallocations for a specific user (as requester or approver)
    @Query("SELECT r FROM Reallocate r WHERE r.requestedBy = :username OR r.approvedBy = :username")
    List<Reallocate> findByUserInvolvement(@Param("username") String username);

    // Count active reallocations by department
    @Query("SELECT COUNT(r) FROM Reallocate r WHERE r.currentDepartment = :department AND r.status = 'APPROVED'")
    Long countActiveReallocationsByDepartment(@Param("department") String department);

    // Count pending approvals
    @Query("SELECT COUNT(r) FROM Reallocate r WHERE r.status = 'PENDING_APPROVAL'")
    Long countPendingApprovals();

    // Find reallocations created within a date range
    @Query("SELECT r FROM Reallocate r WHERE r.createdAt BETWEEN :startDate AND :endDate")
    List<Reallocate> findByCreatedAtBetween(@Param("startDate") LocalDateTime startDate, 
                                           @Param("endDate") LocalDateTime endDate);

    // Find reallocations by time limit
    List<Reallocate> findByTimeLimit(Reallocate.TimeLimit timeLimit);

    // Check if gage has any active reallocation
    @Query("SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END FROM Reallocate r WHERE r.gage.id = :gageId AND r.status IN ('APPROVED', 'PENDING_APPROVAL')")
    boolean existsActiveReallocationByGageId(@Param("gageId") Long gageId);
}
