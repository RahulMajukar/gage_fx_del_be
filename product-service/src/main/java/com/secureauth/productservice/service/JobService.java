package com.secureauth.productservice.service;

import com.secureauth.productservice.dto.JobRequest;
import com.secureauth.productservice.dto.JobResponse;

import java.util.List;

public interface JobService {

    JobResponse createJob(JobRequest jobRequest);

    JobResponse createJobWithGageUsage(JobRequest jobRequest);

    JobResponse updateJob(Long id, JobRequest jobRequest);

    JobResponse getJobById(Long id);

    JobResponse getJobByJobNumber(String jobNumber);

    List<JobResponse> getAllJobs();

    List<JobResponse> getJobsByStatus(JobRequest.Status status);

    List<JobResponse> getJobsByPriority(JobRequest.Priority priority);

    List<JobResponse> getJobsByCreatedBy(String createdBy);

    List<JobResponse> getJobsByAssignedTo(String assignedTo);

    List<JobResponse> getJobsByDepartment(String department);

    List<JobResponse> getJobsByFunction(String functionName);

    List<JobResponse> getJobsByOperation(String operationName);

    List<JobResponse> getJobsByDepartmentFunctionOperation(
            List<String> departments, 
            List<String> functions, 
            List<String> operations);

    List<JobResponse> getJobsByDepartmentFunctionOperationAndPriority(
            List<String> departments, 
            List<String> functions, 
            List<String> operations, 
            JobRequest.Priority priority);

    void deleteJob(Long id);

    long countJobs();

    // Usage tracking methods
    List<JobResponse> getJobsByGageSerialNumber(String serialNumber);
    
    List<JobResponse> getUsageRecordsByOperator(String operatorUsername);
    
    Integer getTotalUsesForGage(String serialNumber);
    
    Integer getTotalDaysUsedForGage(String serialNumber);
    
    // New methods for Plant HOD Dashboard
    List<String> getAllDepartments();
    
    List<String> getAllFunctions();
    
    List<String> getAllOperations();
}
