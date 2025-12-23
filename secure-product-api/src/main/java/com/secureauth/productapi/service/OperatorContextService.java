package com.secureauth.productapi.service;

import com.secureauth.productapi.dto.OperatorContext;
import com.secureauth.productapi.entity.User;
import com.secureauth.productapi.entity.Department;
import com.secureauth.productapi.entity.Function;
import com.secureauth.productapi.entity.Operation;
import com.secureauth.productapi.entity.Role;
import com.secureauth.productapi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OperatorContextService {

    private final UserRepository userRepository;

    /**
     * Get the operator's context for filtering gages
     * @param username The username to get context for
     * @return OperatorContext with departments, functions, and operations
     */
    public OperatorContext getOperatorContext(String username) {
        try {
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found: " + username));

            OperatorContext context = OperatorContext.builder()
                    .userId(user.getId())
                    .username(user.getUsername())
                    .firstName(user.getFirstName())
                    .lastName(user.getLastName())
                    .departments(extractNames(user.getDepartments()))
                    .functions(extractNames(user.getFunctions()))
                    .operations(extractNames(user.getOperations()))
                    .roles(extractRoleNames(user.getRoles()))
                    .build();

            log.info("Retrieved operator context: {}", context.getContextSummary());
            return context;

        } catch (Exception e) {
            log.error("Error retrieving operator context for user {}: {}", username, e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve operator context: " + e.getMessage(), e);
        }
    }

    /**
     * Get the operator's context by user ID
     * @param userId The user ID to get context for
     * @return OperatorContext with departments, functions, and operations
     */
    public OperatorContext getOperatorContextById(Long userId) {
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

            OperatorContext context = OperatorContext.builder()
                    .userId(user.getId())
                    .username(user.getUsername())
                    .firstName(user.getFirstName())
                    .lastName(user.getLastName())
                    .departments(extractNames(user.getDepartments()))
                    .functions(extractNames(user.getFunctions()))
                    .operations(extractNames(user.getOperations()))
                    .roles(extractRoleNames(user.getRoles()))
                    .build();

            log.info("Retrieved operator context by ID: {}", context.getContextSummary());
            return context;

        } catch (Exception e) {
            log.error("Error retrieving operator context for user ID {}: {}", userId, e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve operator context: " + e.getMessage(), e);
        }
    }

    /**
     * Check if a user has access to a specific department
     * @param username The username to check
     * @param department The department to check access for
     * @return true if user has access to the department
     */
    public boolean hasDepartmentAccess(String username, String department) {
        try {
            OperatorContext context = getOperatorContext(username);
            return context.hasDepartment(department);
        } catch (Exception e) {
            log.error("Error checking department access for user {}: {}", username, e.getMessage());
            return false;
        }
    }

    /**
     * Check if a user has access to a specific function
     * @param username The username to check
     * @param function The function to check access for
     * @return true if user has access to the function
     */
    public boolean hasFunctionAccess(String username, String function) {
        try {
            OperatorContext context = getOperatorContext(username);
            return context.hasFunction(function);
        } catch (Exception e) {
            log.error("Error checking function access for user {}: {}", username, e.getMessage());
            return false;
        }
    }

    /**
     * Check if a user has access to a specific operation
     * @param username The username to check
     * @param operation The operation to check access for
     * @return true if user has access to the operation
     */
    public boolean hasOperationAccess(String username, String operation) {
        try {
            OperatorContext context = getOperatorContext(username);
            return context.hasOperation(operation);
        } catch (Exception e) {
            log.error("Error checking operation access for user {}: {}", username, e.getMessage());
            return false;
        }
    }

    /**
     * Get all departments the user has access to
     * @param username The username to get departments for
     * @return Set of department names
     */
    public Set<String> getUserDepartments(String username) {
        try {
            OperatorContext context = getOperatorContext(username);
            return context.getDepartments();
        } catch (Exception e) {
            log.error("Error getting departments for user {}: {}", username, e.getMessage());
            return Set.of();
        }
    }

    /**
     * Get all functions the user has access to
     * @param username The username to get functions for
     * @return Set of function names
     */
    public Set<String> getUserFunctions(String username) {
        try {
            OperatorContext context = getOperatorContext(username);
            return context.getFunctions();
        } catch (Exception e) {
            log.error("Error getting functions for user {}: {}", username, e.getMessage());
            return Set.of();
        }
    }

    /**
     * Get all operations the user has access to
     * @param username The username to get operations for
     * @return Set of operation names
     */
    public Set<String> getUserOperations(String username) {
        try {
            OperatorContext context = getOperatorContext(username);
            return context.getOperations();
        } catch (Exception e) {
            log.error("Error getting operations for user {}: {}", username, e.getMessage());
            return Set.of();
        }
    }

    // Helper methods to extract names from entities
    private Set<String> extractNames(Set<?> entities) {
        if (entities == null) return Set.of();
        
        return entities.stream()
                .map(entity -> {
                    if (entity instanceof Department) return ((Department) entity).getName();
                    if (entity instanceof Function) return ((Function) entity).getName();
                    if (entity instanceof Operation) return ((Operation) entity).getName();
                    return entity.toString();
                })
                .collect(Collectors.toSet());
    }

    private Set<String> extractRoleNames(Set<Role> roles) {
        if (roles == null) return Set.of();
        
        return roles.stream()
                .map(Role::getName)
                .collect(Collectors.toSet());
    }
}

