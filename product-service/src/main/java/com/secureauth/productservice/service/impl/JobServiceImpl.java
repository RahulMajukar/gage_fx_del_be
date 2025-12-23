package com.secureauth.productservice.service.impl;

import com.secureauth.productservice.dto.JobRequest;
import com.secureauth.productservice.dto.JobResponse;
import com.secureauth.productservice.entity.Job;
import com.secureauth.productservice.repository.JobRepository;
import com.secureauth.productservice.service.JobService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class JobServiceImpl implements JobService {

    @Autowired
    private JobRepository jobRepository;

    @Override
    public JobResponse createJob(JobRequest jobRequest) {
        // Check if job with same job number already exists
        if (jobRepository.existsByJobNumber(jobRequest.getJobNumber())) {
            throw new IllegalArgumentException("Job with job number '" + jobRequest.getJobNumber() + "' already exists");
        }

        Job job = mapToJob(jobRequest);
        Job savedJob = jobRepository.save(job);
        return mapToJobResponse(savedJob);
    }

    @Override
    public JobResponse createJobWithGageUsage(JobRequest jobRequest) {
        // Check if job with same job number already exists
        if (jobRepository.existsByJobNumber(jobRequest.getJobNumber())) {
            throw new IllegalArgumentException("Job with job number '" + jobRequest.getJobNumber() + "' already exists");
        }

        // Validate gage usage fields if provided
        if (jobRequest.getGageType() != null && jobRequest.getGageSerialNumber() != null) {
            // Here you could add gage validation logic if needed
            // For now, we'll just create the job with gage usage info
        }

        Job job = mapToJob(jobRequest);
        Job savedJob = jobRepository.save(job);
        return mapToJobResponse(savedJob);
    }

    @Override
    public JobResponse updateJob(Long id, JobRequest jobRequest) {
        Job existingJob = jobRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Job not found with id: " + id));

        // Check if job number is being changed and if new number already exists
        if (!existingJob.getJobNumber().equals(jobRequest.getJobNumber()) && 
            jobRepository.existsByJobNumber(jobRequest.getJobNumber())) {
            throw new IllegalArgumentException("Job with job number '" + jobRequest.getJobNumber() + "' already exists");
        }

        updateJobFromRequest(existingJob, jobRequest);
        Job updatedJob = jobRepository.save(existingJob);
        return mapToJobResponse(updatedJob);
    }

    @Override
    public JobResponse getJobById(Long id) {
        Job job = jobRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Job not found with id: " + id));
        return mapToJobResponse(job);
    }

    @Override
    public JobResponse getJobByJobNumber(String jobNumber) {
        Job job = jobRepository.findByJobNumber(jobNumber)
                .orElseThrow(() -> new IllegalArgumentException("Job not found with job number: " + jobNumber));
        return mapToJobResponse(job);
    }

    @Override
    public List<JobResponse> getAllJobs() {
        return jobRepository.findAll().stream()
                .map(this::mapToJobResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<JobResponse> getJobsByStatus(JobRequest.Status status) {
        Job.Status jobStatus = mapToJobStatus(status);
        return jobRepository.findByStatus(jobStatus).stream()
                .map(this::mapToJobResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<JobResponse> getJobsByPriority(JobRequest.Priority priority) {
        Job.Priority jobPriority = mapToJobPriority(priority);
        return jobRepository.findByPriority(jobPriority).stream()
                .map(this::mapToJobResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<JobResponse> getJobsByCreatedBy(String createdBy) {
        return jobRepository.findByCreatedBy(createdBy).stream()
                .map(this::mapToJobResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<JobResponse> getJobsByAssignedTo(String assignedTo) {
        return jobRepository.findByAssignedTo(assignedTo).stream()
                .map(this::mapToJobResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<JobResponse> getJobsByDepartment(String department) {
        return jobRepository.findByDepartment(department).stream()
                .map(this::mapToJobResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<JobResponse> getJobsByFunction(String functionName) {
        return jobRepository.findByFunctionName(functionName).stream()
                .map(this::mapToJobResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<JobResponse> getJobsByOperation(String operationName) {
        return jobRepository.findByOperationName(operationName).stream()
                .map(this::mapToJobResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<JobResponse> getJobsByDepartmentFunctionOperation(
            List<String> departments, List<String> functions, List<String> operations) {
        return jobRepository.findByDepartmentFunctionOperation(departments, functions, operations).stream()
                .map(this::mapToJobResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<JobResponse> getJobsByDepartmentFunctionOperationAndPriority(
            List<String> departments, List<String> functions, List<String> operations, JobRequest.Priority priority) {
        Job.Priority jobPriority = mapToJobPriority(priority);
        return jobRepository.findByDepartmentFunctionOperationAndPriority(departments, functions, operations, jobPriority).stream()
                .map(this::mapToJobResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteJob(Long id) {
        if (!jobRepository.existsById(id)) {
            throw new IllegalArgumentException("Job not found with id: " + id);
        }
        jobRepository.deleteById(id);
    }

    @Override
    public long countJobs() {
        return jobRepository.count();
    }

    // Usage tracking methods
    @Override
    public List<JobResponse> getJobsByGageSerialNumber(String serialNumber) {
        return jobRepository.findByGageSerialNumber(serialNumber).stream()
                .map(this::mapToJobResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<JobResponse> getUsageRecordsByOperator(String operatorUsername) {
        return jobRepository.findUsageRecordsByOperator(operatorUsername).stream()
                .map(this::mapToJobResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    public Integer getTotalUsesForGage(String serialNumber) {
        List<Job> usageRecords = jobRepository.findByGageSerialNumberAndUsesCountIsNotNull(serialNumber);
        return usageRecords.stream()
                .mapToInt(record -> record.getUsesCount() != null ? record.getUsesCount() : 0)
                .sum();
    }
    
    @Override
    public Integer getTotalDaysUsedForGage(String serialNumber) {
        List<Job> usageRecords = jobRepository.findByGageSerialNumberAndDaysUsedIsNotNull(serialNumber);
        return usageRecords.stream()
                .mapToInt(record -> record.getDaysUsed() != null ? record.getDaysUsed() : 0)
                .sum();
    }

    // Helper methods for mapping
    private Job mapToJob(JobRequest jobRequest) {
        return Job.builder()
                .jobNumber(jobRequest.getJobNumber())
                .jobDescription(jobRequest.getJobDescription())
                .title(jobRequest.getTitle())
                .description(jobRequest.getDescription())
                .status(mapToJobStatus(jobRequest.getStatus()))
                .priority(mapToJobPriority(jobRequest.getPriority()))
                .createdBy(jobRequest.getCreatedBy())
                .assignedTo(jobRequest.getAssignedTo())
                .dueDate(jobRequest.getDueDate())
                .startDate(jobRequest.getStartDate())
                .endDate(jobRequest.getEndDate())
                .department(jobRequest.getDepartment())
                .functionName(jobRequest.getFunctionName())
                .operationName(jobRequest.getOperationName())
                .estimatedDuration(jobRequest.getEstimatedDuration())
                .actualDuration(jobRequest.getActualDuration())
                .location(jobRequest.getLocation())
                .notes(jobRequest.getNotes())
                .gageType(jobRequest.getGageType())
                .gageSerialNumber(jobRequest.getGageSerialNumber())
                .daysUsed(jobRequest.getDaysUsed())
                .usesCount(jobRequest.getUsesCount())
                .operatorUsername(jobRequest.getOperatorUsername())
                .operatorRole(jobRequest.getOperatorRole())
                .operatorFunction(jobRequest.getOperatorFunction())
                .operatorOperation(jobRequest.getOperatorOperation())
                .usageDate(jobRequest.getUsageDate())
                .usageCount(jobRequest.getUsageCount())
                .usageNotes(jobRequest.getUsageNotes())
                .tags(jobRequest.getTags() != null ? jobRequest.getTags() : List.of())
                .attachments(jobRequest.getAttachments() != null ? jobRequest.getAttachments() : List.of())
                .build();
    }

    private JobResponse mapToJobResponse(Job job) {
        return JobResponse.builder()
                .id(job.getId())
                .jobNumber(job.getJobNumber())
                .jobDescription(job.getJobDescription())
                .title(job.getTitle())
                .description(job.getDescription())
                .status(mapToJobResponseStatus(job.getStatus()))
                .priority(mapToJobResponsePriority(job.getPriority()))
                .createdBy(job.getCreatedBy())
                .assignedTo(job.getAssignedTo())
                .dueDate(job.getDueDate())
                .startDate(job.getStartDate())
                .endDate(job.getEndDate())
                .department(job.getDepartment())
                .functionName(job.getFunctionName())
                .operationName(job.getOperationName())
                .estimatedDuration(job.getEstimatedDuration())
                .actualDuration(job.getActualDuration())
                .location(job.getLocation())
                .notes(job.getNotes())
                .gageType(job.getGageType())
                .gageSerialNumber(job.getGageSerialNumber())
                .daysUsed(job.getDaysUsed())
                .usesCount(job.getUsesCount())
                .operatorUsername(job.getOperatorUsername())
                .operatorRole(job.getOperatorRole())
                .operatorFunction(job.getOperatorFunction())
                .operatorOperation(job.getOperatorOperation())
                .usageDate(job.getUsageDate())
                .usageCount(job.getUsageCount())
                .usageNotes(job.getUsageNotes())
                .tags(job.getTags())
                .attachments(job.getAttachments())
                .createdAt(job.getCreatedAt())
                .updatedAt(job.getUpdatedAt())
                .build();
    }

    private void updateJobFromRequest(Job job, JobRequest jobRequest) {
        job.setJobNumber(jobRequest.getJobNumber());
        job.setJobDescription(jobRequest.getJobDescription());
        job.setTitle(jobRequest.getTitle());
        job.setDescription(jobRequest.getDescription());
        if (jobRequest.getStatus() != null) {
            job.setStatus(mapToJobStatus(jobRequest.getStatus()));
        }
        if (jobRequest.getPriority() != null) {
            job.setPriority(mapToJobPriority(jobRequest.getPriority()));
        }
        job.setAssignedTo(jobRequest.getAssignedTo());
        job.setDueDate(jobRequest.getDueDate());
        job.setStartDate(jobRequest.getStartDate());
        job.setEndDate(jobRequest.getEndDate());
        job.setDepartment(jobRequest.getDepartment());
        job.setFunctionName(jobRequest.getFunctionName());
        job.setOperationName(jobRequest.getOperationName());
        job.setEstimatedDuration(jobRequest.getEstimatedDuration());
        job.setActualDuration(jobRequest.getActualDuration());
        job.setLocation(jobRequest.getLocation());
        job.setNotes(jobRequest.getNotes());
        job.setGageType(jobRequest.getGageType());
        job.setGageSerialNumber(jobRequest.getGageSerialNumber());
        job.setDaysUsed(jobRequest.getDaysUsed());
        job.setUsesCount(jobRequest.getUsesCount());
        job.setOperatorUsername(jobRequest.getOperatorUsername());
        job.setOperatorRole(jobRequest.getOperatorRole());
        job.setOperatorFunction(jobRequest.getOperatorFunction());
        job.setOperatorOperation(jobRequest.getOperatorOperation());
        job.setUsageDate(jobRequest.getUsageDate());
        job.setUsageCount(jobRequest.getUsageCount());
        job.setUsageNotes(jobRequest.getUsageNotes());
        if (jobRequest.getTags() != null) {
            job.setTags(jobRequest.getTags());
        }
        if (jobRequest.getAttachments() != null) {
            job.setAttachments(jobRequest.getAttachments());
        }
    }

    private Job.Status mapToJobStatus(JobRequest.Status status) {
        if (status == null) return Job.Status.OPEN;
        return switch (status) {
            case OPEN -> Job.Status.OPEN;
            case IN_PROGRESS -> Job.Status.IN_PROGRESS;
            case COMPLETED -> Job.Status.COMPLETED;
            case CANCELLED -> Job.Status.CANCELLED;
            case ON_HOLD -> Job.Status.ON_HOLD;
        };
    }

    private Job.Priority mapToJobPriority(JobRequest.Priority priority) {
        if (priority == null) return Job.Priority.MEDIUM;
        return switch (priority) {
            case LOW -> Job.Priority.LOW;
            case MEDIUM -> Job.Priority.MEDIUM;
            case HIGH -> Job.Priority.HIGH;
            case URGENT -> Job.Priority.URGENT;
        };
    }

    private JobResponse.Status mapToJobResponseStatus(Job.Status status) {
        return switch (status) {
            case OPEN -> JobResponse.Status.OPEN;
            case IN_PROGRESS -> JobResponse.Status.IN_PROGRESS;
            case COMPLETED -> JobResponse.Status.COMPLETED;
            case CANCELLED -> JobResponse.Status.CANCELLED;
            case ON_HOLD -> JobResponse.Status.ON_HOLD;
        };
    }

    private JobResponse.Priority mapToJobResponsePriority(Job.Priority priority) {
        return switch (priority) {
            case LOW -> JobResponse.Priority.LOW;
            case MEDIUM -> JobResponse.Priority.MEDIUM;
            case HIGH -> JobResponse.Priority.HIGH;
            case URGENT -> JobResponse.Priority.URGENT;
        };
    }

    @Override
    public List<String> getAllDepartments() {
        return jobRepository.findAll().stream()
                .map(Job::getDepartment)
                .filter(department -> department != null && !department.trim().isEmpty())
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    @Override
    public List<String> getAllFunctions() {
        return jobRepository.findAll().stream()
                .map(Job::getFunctionName)
                .filter(function -> function != null && !function.trim().isEmpty())
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    @Override
    public List<String> getAllOperations() {
        return jobRepository.findAll().stream()
                .map(Job::getOperationName)
                .filter(operation -> operation != null && !operation.trim().isEmpty())
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }
}
