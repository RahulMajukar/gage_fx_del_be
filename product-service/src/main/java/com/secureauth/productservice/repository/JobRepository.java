package com.secureauth.productservice.repository;

import com.secureauth.productservice.entity.Job;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JobRepository extends JpaRepository<Job, Long> {

    Optional<Job> findByJobNumber(String jobNumber);

    boolean existsByJobNumber(String jobNumber);

    List<Job> findByStatus(Job.Status status);

    List<Job> findByPriority(Job.Priority priority);

    List<Job> findByCreatedBy(String createdBy);

    List<Job> findByAssignedTo(String assignedTo);

    List<Job> findByDepartment(String department);

    List<Job> findByFunctionName(String functionName);

    List<Job> findByOperationName(String operationName);

    @Query("SELECT j FROM Job j WHERE " +
           "(:departments IS NULL OR j.department IN :departments) AND " +
           "(:functions IS NULL OR j.functionName IN :functions) AND " +
           "(:operations IS NULL OR j.operationName IN :operations)")
    List<Job> findByDepartmentFunctionOperation(
            @Param("departments") List<String> departments,
            @Param("functions") List<String> functions,
            @Param("operations") List<String> operations);

    @Query("SELECT j FROM Job j WHERE " +
           "(:departments IS NULL OR j.department IN :departments) AND " +
           "(:functions IS NULL OR j.functionName IN :functions) AND " +
           "(:operations IS NULL OR j.operationName IN :operations) AND " +
           "j.priority = :priority")
    List<Job> findByDepartmentFunctionOperationAndPriority(
            @Param("departments") List<String> departments,
            @Param("functions") List<String> functions,
            @Param("operations") List<String> operations,
            @Param("priority") Job.Priority priority);

    // Usage tracking queries
    @Query("SELECT j FROM Job j WHERE j.gageSerialNumber = :serialNumber AND j.usesCount IS NOT NULL")
    List<Job> findByGageSerialNumberAndUsesCountIsNotNull(@Param("serialNumber") String serialNumber);
    
    @Query("SELECT j FROM Job j WHERE j.gageSerialNumber = :serialNumber AND j.daysUsed IS NOT NULL")
    List<Job> findByGageSerialNumberAndDaysUsedIsNotNull(@Param("serialNumber") String serialNumber);
    
    @Query("SELECT j FROM Job j WHERE j.operatorUsername = :username AND j.usesCount IS NOT NULL ORDER BY j.usageDate DESC")
    List<Job> findUsageRecordsByOperator(@Param("username") String username);
    
    @Query("SELECT j FROM Job j WHERE j.gageSerialNumber = :serialNumber")
    List<Job> findByGageSerialNumber(@Param("serialNumber") String serialNumber);
}
