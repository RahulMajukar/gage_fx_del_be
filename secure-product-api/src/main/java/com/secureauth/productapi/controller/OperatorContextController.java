package com.secureauth.productapi.controller;

import com.secureauth.productapi.dto.OperatorContext;
import com.secureauth.productapi.service.OperatorContextService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("/api/operator/context")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class OperatorContextController {

    private final OperatorContextService operatorContextService;

    /**
     * Get operator context by username
     * @param username The username to get context for
     * @return OperatorContext with departments, functions, and operations
     */
    @GetMapping("/{username}")
    public ResponseEntity<OperatorContext> getOperatorContext(@PathVariable String username) {
        try {
            log.info("Getting operator context for username: {}", username);
            
            OperatorContext context = operatorContextService.getOperatorContext(username);
            
            log.info("Successfully retrieved operator context: {}", context.getContextSummary());
            return ResponseEntity.ok(context);

        } catch (Exception e) {
            log.error("Error getting operator context for username {}: {}", username, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get operator context by user ID
     * @param userId The user ID to get context for
     * @return OperatorContext with departments, functions, and operations
     */
    @GetMapping("/id/{userId}")
    public ResponseEntity<OperatorContext> getOperatorContextById(@PathVariable Long userId) {
        try {
            log.info("Getting operator context for user ID: {}", userId);
            
            OperatorContext context = operatorContextService.getOperatorContextById(userId);
            
            log.info("Successfully retrieved operator context by ID: {}", context.getContextSummary());
            return ResponseEntity.ok(context);

        } catch (Exception e) {
            log.error("Error getting operator context for user ID {}: {}", userId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Check if user has department access
     * @param username The username to check
     * @param department The department to check access for
     * @return true if user has access to the department
     */
    @GetMapping("/{username}/department-access")
    public ResponseEntity<Boolean> hasDepartmentAccess(
            @PathVariable String username,
            @RequestParam String department) {
        
        try {
            log.info("Checking department access for user {} to department {}", username, department);
            
            boolean hasAccess = operatorContextService.hasDepartmentAccess(username, department);
            
            log.info("User {} {} access to department {}", username, hasAccess ? "has" : "does not have", department);
            return ResponseEntity.ok(hasAccess);

        } catch (Exception e) {
            log.error("Error checking department access for user {}: {}", username, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Check if user has function access
     * @param username The username to check
     * @param function The function to check access for
     * @return true if user has access to the function
     */
    @GetMapping("/{username}/function-access")
    public ResponseEntity<Boolean> hasFunctionAccess(
            @PathVariable String username,
            @RequestParam String function) {
        
        try {
            log.info("Checking function access for user {} to function {}", username, function);
            
            boolean hasAccess = operatorContextService.hasFunctionAccess(username, function);
            
            log.info("User {} {} access to function {}", username, hasAccess ? "has" : "does not have", function);
            return ResponseEntity.ok(hasAccess);

        } catch (Exception e) {
            log.error("Error checking function access for user {}: {}", username, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Check if user has operation access
     * @param username The username to check
     * @param operation The operation to check access for
     * @return true if user has access to the operation
     */
    @GetMapping("/{username}/operation-access")
    public ResponseEntity<Boolean> hasOperationAccess(
            @PathVariable String username,
            @RequestParam String operation) {
        
        try {
            log.info("Checking operation access for user {} to operation {}", username, operation);
            
            boolean hasAccess = operatorContextService.hasOperationAccess(username, operation);
            
            log.info("User {} {} access to operation {}", username, hasAccess ? "has" : "does not have", operation);
            return ResponseEntity.ok(hasAccess);

        } catch (Exception e) {
            log.error("Error checking operation access for user {}: {}", username, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get all departments for a user
     * @param username The username to get departments for
     * @return Set of department names
     */
    @GetMapping("/{username}/departments")
    public ResponseEntity<Set<String>> getUserDepartments(@PathVariable String username) {
        try {
            log.info("Getting departments for user: {}", username);
            
            Set<String> departments = operatorContextService.getUserDepartments(username);
            
            log.info("User {} has {} departments: {}", username, departments.size(), departments);
            return ResponseEntity.ok(departments);

        } catch (Exception e) {
            log.error("Error getting departments for user {}: {}", username, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get all functions for a user
     * @param username The username to get functions for
     * @return Set of function names
     */
    @GetMapping("/{username}/functions")
    public ResponseEntity<Set<String>> getUserFunctions(@PathVariable String username) {
        try {
            log.info("Getting functions for user: {}", username);
            
            Set<String> functions = operatorContextService.getUserFunctions(username);
            
            log.info("User {} has {} functions: {}", username, functions.size(), functions);
            return ResponseEntity.ok(functions);

        } catch (Exception e) {
            log.error("Error getting functions for user {}: {}", username, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get all operations for a user
     * @param username The username to get operations for
     * @return Set of operation names
     */
    @GetMapping("/{username}/operations")
    public ResponseEntity<Set<String>> getUserOperations(@PathVariable String username) {
        try {
            log.info("Getting operations for user: {}", username);
            
            Set<String> operations = operatorContextService.getUserOperations(username);
            
            log.info("User {} has {} operations: {}", username, operations.size(), operations);
            return ResponseEntity.ok(operations);

        } catch (Exception e) {
            log.error("Error getting operations for user {}: {}", username, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Operator Context Controller is running");
    }
}

