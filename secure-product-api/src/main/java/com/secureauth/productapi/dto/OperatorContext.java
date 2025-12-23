package com.secureauth.productapi.dto;

import lombok.*;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OperatorContext {
    
    private Long userId;
    private String username;
    private String firstName;
    private String lastName;
    
    // Operator's context - these determine which gages they can see
    private Set<String> departments;      // e.g., ["Engineering", "Tooling"]
    private Set<String> functions;        // e.g., ["F1", "F2", "F3"]
    private Set<String> operations;       // e.g., ["OT1", "OT2", "OT3"]
    
    // Role information
    private Set<String> roles;            // e.g., ["OPERATOR", "SUPERVISOR"]
    
    // Helper methods for filtering logic
    public boolean hasDepartment(String department) {
        return departments != null && departments.contains(department);
    }
    
    public boolean hasFunction(String function) {
        return functions != null && functions.contains(function);
    }
    
    public boolean hasOperation(String operation) {
        return operations != null && operations.contains(operation);
    }
    
    public boolean isOperator() {
        return roles != null && roles.contains("OPERATOR");
    }
    
    public boolean isSupervisor() {
        return roles != null && roles.contains("SUPERVISOR");
    }
    
    public boolean isManager() {
        return roles != null && roles.contains("MANAGER");
    }
    
    // Get context summary for logging/debugging
    public String getContextSummary() {
        return String.format("User: %s, Depts: %s, Functions: %s, Operations: %s", 
            username, departments, functions, operations);
    }
}

