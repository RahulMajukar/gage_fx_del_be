package com.secureauth.productservice.controller;

import com.secureauth.productservice.dto.JobRequest;
import com.secureauth.productservice.dto.JobResponse;
import com.secureauth.productservice.service.JobService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/jobs")
public class JobController {

    @Autowired
    private JobService jobService;

    @PostMapping
    public ResponseEntity<JobResponse> createJob(@Valid @RequestBody JobRequest jobRequest) {
        try {
            JobResponse createdJob = jobService.createJob(jobRequest);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdJob);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/with-gage-usage")
    public ResponseEntity<JobResponse> createJobWithGageUsage(@Valid @RequestBody JobRequest jobRequest) {
        try {
            JobResponse createdJob = jobService.createJobWithGageUsage(jobRequest);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdJob);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<JobResponse> updateJob(@PathVariable Long id, @Valid @RequestBody JobRequest jobRequest) {
        try {
            JobResponse updatedJob = jobService.updateJob(id, jobRequest);
            return ResponseEntity.ok(updatedJob);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<JobResponse> getJobById(@PathVariable Long id) {
        try {
            JobResponse job = jobService.getJobById(id);
            return ResponseEntity.ok(job);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/job-number/{jobNumber}")
    public ResponseEntity<JobResponse> getJobByJobNumber(@PathVariable String jobNumber) {
        try {
            JobResponse job = jobService.getJobByJobNumber(jobNumber);
            return ResponseEntity.ok(job);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping
    public ResponseEntity<List<JobResponse>> getAllJobs() {
        List<JobResponse> jobs = jobService.getAllJobs();
        return ResponseEntity.ok(jobs);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<JobResponse>> getJobsByStatus(@PathVariable JobRequest.Status status) {
        List<JobResponse> jobs = jobService.getJobsByStatus(status);
        return ResponseEntity.ok(jobs);
    }

    @GetMapping("/priority/{priority}")
    public ResponseEntity<List<JobResponse>> getJobsByPriority(@PathVariable JobRequest.Priority priority) {
        List<JobResponse> jobs = jobService.getJobsByPriority(priority);
        return ResponseEntity.ok(jobs);
    }

    @GetMapping("/created-by/{createdBy}")
    public ResponseEntity<List<JobResponse>> getJobsByCreatedBy(@PathVariable String createdBy) {
        List<JobResponse> jobs = jobService.getJobsByCreatedBy(createdBy);
        return ResponseEntity.ok(jobs);
    }

    @GetMapping("/assigned-to/{assignedTo}")
    public ResponseEntity<List<JobResponse>> getJobsByAssignedTo(@PathVariable String assignedTo) {
        List<JobResponse> jobs = jobService.getJobsByAssignedTo(assignedTo);
        return ResponseEntity.ok(jobs);
    }

    @GetMapping("/department/{department}")
    public ResponseEntity<List<JobResponse>> getJobsByDepartment(@PathVariable String department) {
        List<JobResponse> jobs = jobService.getJobsByDepartment(department);
        return ResponseEntity.ok(jobs);
    }

    @GetMapping("/function/{functionName}")
    public ResponseEntity<List<JobResponse>> getJobsByFunction(@PathVariable String functionName) {
        List<JobResponse> jobs = jobService.getJobsByFunction(functionName);
        return ResponseEntity.ok(jobs);
    }

    @GetMapping("/operation/{operationName}")
    public ResponseEntity<List<JobResponse>> getJobsByOperation(@PathVariable String operationName) {
        List<JobResponse> jobs = jobService.getJobsByOperation(operationName);
        return ResponseEntity.ok(jobs);
    }

    @GetMapping("/operator-filtered")
    public ResponseEntity<List<JobResponse>> getOperatorFilteredJobs(
            @RequestParam List<String> departments,
            @RequestParam(required = false) List<String> functions,
            @RequestParam(required = false) List<String> operations) {
        List<JobResponse> jobs = jobService.getJobsByDepartmentFunctionOperation(
                departments, 
                functions != null ? functions : List.of(), 
                operations != null ? operations : List.of());
        return ResponseEntity.ok(jobs);
    }

    @GetMapping("/operator-filtered/priority")
    public ResponseEntity<List<JobResponse>> getOperatorFilteredJobsByPriority(
            @RequestParam List<String> departments,
            @RequestParam(required = false) List<String> functions,
            @RequestParam(required = false) List<String> operations,
            @RequestParam JobRequest.Priority priority) {
        List<JobResponse> jobs = jobService.getJobsByDepartmentFunctionOperationAndPriority(
                departments, 
                functions != null ? functions : List.of(), 
                operations != null ? operations : List.of(),
                priority);
        return ResponseEntity.ok(jobs);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteJob(@PathVariable Long id) {
        try {
            jobService.deleteJob(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/count")
    public ResponseEntity<Long> countJobs() {
        long count = jobService.countJobs();
        return ResponseEntity.ok(count);
    }

    // Usage tracking endpoints
    @GetMapping("/gage/{serialNumber}")
    public ResponseEntity<List<JobResponse>> getJobsByGageSerialNumber(@PathVariable String serialNumber) {
        try {
            List<JobResponse> jobs = jobService.getJobsByGageSerialNumber(serialNumber);
            return ResponseEntity.ok(jobs);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/operator/{operatorUsername}")
    public ResponseEntity<List<JobResponse>> getUsageRecordsByOperator(@PathVariable String operatorUsername) {
        try {
            List<JobResponse> jobs = jobService.getUsageRecordsByOperator(operatorUsername);
            return ResponseEntity.ok(jobs);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/usage/total-uses/{serialNumber}")
    public ResponseEntity<Integer> getTotalUsesForGage(@PathVariable String serialNumber) {
        try {
            Integer totalUses = jobService.getTotalUsesForGage(serialNumber);
            return ResponseEntity.ok(totalUses);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/usage/total-days/{serialNumber}")
    public ResponseEntity<Integer> getTotalDaysUsedForGage(@PathVariable String serialNumber) {
        try {
            Integer totalDays = jobService.getTotalDaysUsedForGage(serialNumber);
            return ResponseEntity.ok(totalDays);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // New endpoints for Plant HOD Dashboard
    @GetMapping("/departments")
    public ResponseEntity<List<String>> getAllDepartments() {
        try {
            List<String> departments = jobService.getAllDepartments();
            return ResponseEntity.ok(departments);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/functions")
    public ResponseEntity<List<String>> getAllFunctions() {
        try {
            List<String> functions = jobService.getAllFunctions();
            return ResponseEntity.ok(functions);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/operations")
    public ResponseEntity<List<String>> getAllOperations() {
        try {
            List<String> operations = jobService.getAllOperations();
            return ResponseEntity.ok(operations);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
