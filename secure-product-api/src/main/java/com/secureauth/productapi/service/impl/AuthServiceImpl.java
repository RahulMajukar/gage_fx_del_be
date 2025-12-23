package com.secureauth.productapi.service.impl;

import com.secureauth.productapi.dto.JwtResponse;
import com.secureauth.productapi.dto.LoginRequest;
import com.secureauth.productapi.dto.SignupRequest;
import com.secureauth.productapi.entity.Role;
import com.secureauth.productapi.entity.User;
import com.secureauth.productapi.repository.RoleRepository;
import com.secureauth.productapi.repository.UserRepository;
import com.secureauth.productapi.security.JwtTokenProvider;
import com.secureauth.productapi.service.interfaces.AuthService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.secureauth.productapi.entity.Department;
import com.secureauth.productapi.entity.Function;
import com.secureauth.productapi.entity.Operation;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class AuthServiceImpl implements AuthService {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Transactional
    public JwtResponse authenticateUser(LoginRequest loginRequest) {
        // Support login by username or email
        User user = userRepository.findByUsername(loginRequest.getUsername())
                .or(() -> userRepository.findByEmail(loginRequest.getUsername()))
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + loginRequest.getUsername()))
                ;

        // Spring Security authentication
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Generate JWT
        String jwt = tokenProvider.generateToken(authentication.getName(), authentication.getAuthorities());

        // Access lazy collections safely
        Set<String> roles = user.getRoles().stream().map(Role::getName).collect(Collectors.toSet());
        Set<String> departments = user.getDepartments().stream().map(Department::getName).collect(Collectors.toSet());
        Set<String> functions = user.getFunctions().stream().map(Function::getName).collect(Collectors.toSet());
        Set<String> operations = user.getOperations().stream().map(Operation::getName).collect(Collectors.toSet());

        JwtResponse jwtResponse = new JwtResponse();
        jwtResponse.setToken(jwt);
        jwtResponse.setType("Bearer");
        jwtResponse.setUsername(user.getUsername());
        jwtResponse.setEmail(user.getEmail());
        jwtResponse.setProfileImage(user.getProfileImage());
        jwtResponse.setRoles(roles);
        jwtResponse.setDepartments(departments);
        jwtResponse.setFunctions(functions);
        jwtResponse.setOperations(operations);

        return jwtResponse;
    }

    @Override
    public String registerUser(SignupRequest signupRequest) {
        if (userRepository.existsByUsername(signupRequest.getUsername())) {
            throw new RuntimeException("Username is already taken!");
        }

        User user = new User();
        user.setUsername(signupRequest.getUsername());
        user.setPassword(passwordEncoder.encode(signupRequest.getPassword()));

        Set<Role> roles = new HashSet<>();
        if (signupRequest.getRole() == null || signupRequest.getRole().isEmpty()) {
            Role userRole = roleRepository.findByName("ROLE_USER")
                    .orElseThrow(() -> new RuntimeException("Role USER not found"));
            roles.add(userRole);
        } else {
            for (String roleStr : signupRequest.getRole()) {
                Role role = roleRepository.findByName("ROLE_" + roleStr.toUpperCase())
                        .orElseThrow(() -> new RuntimeException("Role " + roleStr + " not found"));
                roles.add(role);
            }
        }
        user.setRoles(roles);

        userRepository.save(user);

        return "User registered successfully!";
    }
    @Override
    public JwtResponse getUserInfoByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        JwtResponse jwtResponse = new JwtResponse();
        jwtResponse.setUsername(user.getUsername());
        jwtResponse.setRoles(user.getRoles().stream().map(Role::getName).collect(Collectors.toSet()));
        jwtResponse.setDepartments(user.getDepartments().stream().map(Department::getName).collect(Collectors.toSet()));
        jwtResponse.setFunctions(user.getFunctions().stream().map(Function::getName).collect(Collectors.toSet()));
        jwtResponse.setOperations(user.getOperations().stream().map(Operation::getName).collect(Collectors.toSet()));
        return jwtResponse;
    }
}
