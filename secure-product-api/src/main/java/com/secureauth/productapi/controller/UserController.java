package com.secureauth.productapi.controller;

import com.secureauth.productapi.dto.UserCreateRequest;
import com.secureauth.productapi.dto.UserResponse;
import com.secureauth.productapi.entity.*;
import com.secureauth.productapi.repository.*;
import com.secureauth.productapi.service.interfaces.EmailService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.security.SecureRandom;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired private UserRepository userRepository;
    @Autowired private RoleRepository roleRepository;
    @Autowired private DepartmentRepository departmentRepository;
    @Autowired private FunctionRepository functionRepository;
    @Autowired private OperationRepository operationRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private EmailService emailService;

    @GetMapping
    public List<UserResponse> list() {
        return userRepository.findAll().stream().map(this::toResponse).toList();
    }

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody UserCreateRequest req) {
        if (userRepository.existsByUsername(req.getUsername())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("User already exists");
        }

        boolean adminSetsPassword = Boolean.TRUE.equals(req.getAdminSetsPassword());
        String rawPassword = adminSetsPassword && req.getPassword() != null && !req.getPassword().isBlank()
                ? req.getPassword()
                : generatePassword();

        User user = new User();
        user.setUsername(req.getUsername());
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setFirstName(req.getFirstName());
        user.setLastName(req.getLastName());
        user.setCountryCode(req.getCountryCode());
        user.setPhone(req.getPhone());
        if (req.getEmail() != null && !req.getEmail().isBlank()) {
            user.setEmail(req.getEmail());
        }

        // Map enum fields — ✅ FIXED: Use User.Location, etc.
        if (req.getLocation() != null && !req.getLocation().isBlank()) {
            user.setLocation(User.Location.valueOf(req.getLocation().toUpperCase()));
        }
        if (req.getPlant() != null && !req.getPlant().isBlank()) {
            user.setPlant(User.Plant.valueOf(req.getPlant().toUpperCase()));
        }
        if (req.getArea() != null && !req.getArea().isBlank()) {
            user.setArea(User.Area.valueOf(req.getArea().toUpperCase()));
        }

        if (req.getRoleId() != null) {
            Role role = roleRepository.findById(req.getRoleId()).orElseThrow();
            user.setRoles(Set.of(role));
        }

        if (req.getDepartmentIds() != null && !req.getDepartmentIds().isEmpty()) {
            Set<Department> departments = req.getDepartmentIds().stream()
                    .map(id -> departmentRepository.findById(id).orElseThrow())
                    .collect(Collectors.toSet());
            user.setDepartments(departments);
        }

        if (req.getFunctionIds() != null && !req.getFunctionIds().isEmpty()) {
            Set<Function> functions = req.getFunctionIds().stream()
                    .map(id -> functionRepository.findById(id).orElseThrow())
                    .collect(Collectors.toSet());
            user.setFunctions(functions);
        }

        if (req.getOperationIds() != null && !req.getOperationIds().isEmpty()) {
            Set<Operation> operations = req.getOperationIds().stream()
                    .map(id -> operationRepository.findById(id).orElseThrow())
                    .collect(Collectors.toSet());
            user.setOperations(operations);
        }

        if (req.getProfileImage() != null && !req.getProfileImage().isBlank()) {
            user.setProfileImage(req.getProfileImage());
        }

        user = userRepository.save(user);

        // Send credentials email (non-blocking for failures) only if email is provided and admin didn't set password manually
        if (!adminSetsPassword && user.getEmail() != null && !user.getEmail().isBlank()) {
            try {
                emailService.sendCredentials(user.getEmail(), user.getUsername(), rawPassword);
            } catch (Exception ignored) { }
        }

        return ResponseEntity.ok(toResponse(user));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @Valid @RequestBody UserCreateRequest req) {
        return userRepository.findById(id)
                .map(user -> {
                    // Update non-credential fields; username is assumed immutable for simplicity
                    if (req.getFirstName() != null && !req.getFirstName().isBlank()) {
                        user.setFirstName(req.getFirstName());
                    }
                    if (req.getLastName() != null && !req.getLastName().isBlank()) {
                        user.setLastName(req.getLastName());
                    }
                    if (req.getCountryCode() != null && !req.getCountryCode().isBlank()) {
                        user.setCountryCode(req.getCountryCode());
                    }
                    if (req.getPhone() != null && !req.getPhone().isBlank()) {
                        user.setPhone(req.getPhone());
                    }
                    if (req.getEmail() != null && !req.getEmail().isBlank()) {
                        user.setEmail(req.getEmail());
                    }
                    if (req.getProfileImage() != null && !req.getProfileImage().isBlank()) {
                        user.setProfileImage(req.getProfileImage());
                    }

                    // Update enum fields — ✅ FIXED
                    if (req.getLocation() != null && !req.getLocation().isBlank()) {
                        user.setLocation(User.Location.valueOf(req.getLocation().toUpperCase()));
                    }
                    if (req.getPlant() != null && !req.getPlant().isBlank()) {
                        user.setPlant(User.Plant.valueOf(req.getPlant().toUpperCase()));
                    }
                    if (req.getArea() != null && !req.getArea().isBlank()) {
                        user.setArea(User.Area.valueOf(req.getArea().toUpperCase()));
                    }

                    // Update role if provided
                    if (req.getRoleId() != null) {
                        Role role = roleRepository.findById(req.getRoleId()).orElseThrow();
                        user.setRoles(Set.of(role));
                    }

                    // Update departments if provided
                    if (req.getDepartmentIds() != null && !req.getDepartmentIds().isEmpty()) {
                        Set<Department> departments = req.getDepartmentIds().stream()
                                .map(deptId -> departmentRepository.findById(deptId).orElseThrow())
                                .collect(Collectors.toSet());
                        user.setDepartments(departments);
                    }

                    // Update functions if provided
                    if (req.getFunctionIds() != null && !req.getFunctionIds().isEmpty()) {
                        Set<Function> functions = req.getFunctionIds().stream()
                                .map(funcId -> functionRepository.findById(funcId).orElseThrow())
                                .collect(Collectors.toSet());
                        user.setFunctions(functions);
                    }

                    // Update operations if provided
                    if (req.getOperationIds() != null && !req.getOperationIds().isEmpty()) {
                        Set<Operation> operations = req.getOperationIds().stream()
                                .map(opId -> operationRepository.findById(opId).orElseThrow())
                                .collect(Collectors.toSet());
                        user.setOperations(operations);
                    }

                    // Optional password update (only if adminSetsPassword is true and password is provided)
                    boolean adminSetsPassword = Boolean.TRUE.equals(req.getAdminSetsPassword());
                    if (adminSetsPassword && req.getPassword() != null && !req.getPassword().isBlank()) {
                        user.setPassword(passwordEncoder.encode(req.getPassword()));
                    }

                    user = userRepository.save(user);
                    return ResponseEntity.ok(toResponse(user));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}")
    public ResponseEntity<?> partialUpdate(@PathVariable Long id, @RequestBody java.util.Map<String, Object> updates) {
        return userRepository.findById(id)
                .map(user -> {
                    if (updates.containsKey("firstName")) {
                        String v = (String) updates.get("firstName");
                        if (v != null && !v.isBlank()) user.setFirstName(v);
                    }
                    if (updates.containsKey("lastName")) {
                        String v = (String) updates.get("lastName");
                        if (v != null && !v.isBlank()) user.setLastName(v);
                    }
                    if (updates.containsKey("email")) {
                        String v = (String) updates.get("email");
                        if (v != null && !v.isBlank()) user.setEmail(v);
                    }
                    if (updates.containsKey("countryCode")) {
                        String v = (String) updates.get("countryCode");
                        if (v != null && !v.isBlank()) user.setCountryCode(v);
                    }
                    if (updates.containsKey("phone")) {
                        String v = (String) updates.get("phone");
                        if (v != null && !v.isBlank()) user.setPhone(v);
                    }
                    if (updates.containsKey("profileImage")) {
                        Object val = updates.get("profileImage");
                        // FIX: Properly handle profile image updates - accept both string and null
                        if (val instanceof String img && !img.isBlank()) {
                            user.setProfileImage(img);
                        } else if (val == null) {
                            user.setProfileImage(null);
                        } else if ("".equals(val)) {
                            user.setProfileImage(null);
                        }
                    }

                    // Handle enum fields in partial update — ✅ FIXED
                    if (updates.containsKey("location")) {
                        String loc = (String) updates.get("location");
                        if (loc != null && !loc.isBlank()) {
                            user.setLocation(User.Location.valueOf(loc.toUpperCase()));
                        }
                    }
                    if (updates.containsKey("plant")) {
                        String pl = (String) updates.get("plant");
                        if (pl != null && !pl.isBlank()) {
                            user.setPlant(User.Plant.valueOf(pl.toUpperCase()));
                        }
                    }
                    if (updates.containsKey("area")) {
                        String ar = (String) updates.get("area");
                        if (ar != null && !ar.isBlank()) {
                            user.setArea(User.Area.valueOf(ar.toUpperCase()));
                        }
                    }

                    user = userRepository.save(user);
                    return ResponseEntity.ok(toResponse(user));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/me")
    public ResponseEntity<?> me() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return userRepository.findByUsername(auth.getName())
                .map(u -> ResponseEntity.ok(toResponse(u)))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<?> toggleStatus(@PathVariable Long id, @RequestParam boolean active) {
        return userRepository.findById(id)
                .map(u -> {
                    u.setIsActive(active);
                    userRepository.save(u);
                    return ResponseEntity.ok().build();
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    private String generatePassword() {
        final String chars = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz23456789@#$%&";
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 12; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

    private UserResponse toResponse(User user) {
        UserResponse res = new UserResponse();
        res.setId(user.getId());
        res.setUsername(user.getUsername());
        res.setEmail(user.getEmail());
        res.setFirstName(user.getFirstName());
        res.setLastName(user.getLastName());
        res.setCountryCode(user.getCountryCode());
        res.setPhone(user.getPhone());
        res.setActive(Boolean.TRUE.equals(user.getIsActive()));
        res.setRole(user.getRoles() != null && !user.getRoles().isEmpty() ? user.getRoles().iterator().next().getName() : null);
        res.setDepartments(user.getDepartments() != null ? user.getDepartments().stream().map(Department::getName).collect(Collectors.toSet()) : Set.of());
        res.setFunctions(user.getFunctions() != null ? user.getFunctions().stream().map(Function::getName).collect(Collectors.toSet()) : Set.of());
        res.setOperations(user.getOperations() != null ? user.getOperations().stream().map(Operation::getName).collect(Collectors.toSet()) : Set.of());
        res.setProfileImage(user.getProfileImage());

        // Map enum fields to string names — ✅ Already correct (uses user.getLocation().name())
        res.setLocation(user.getLocation() != null ? user.getLocation().name() : null);
        res.setPlant(user.getPlant() != null ? user.getPlant().name() : null);
        res.setArea(user.getArea() != null ? user.getArea().name() : null);

        return res;
    }
}