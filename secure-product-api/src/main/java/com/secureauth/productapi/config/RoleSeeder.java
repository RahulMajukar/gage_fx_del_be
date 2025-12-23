package com.secureauth.productapi.config;

import com.secureauth.productapi.entity.Role;
import com.secureauth.productapi.repository.RoleRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RoleSeeder {

    @Autowired
    private RoleRepository roleRepository;

    @PostConstruct
    public void seedRoles() {
        if (roleRepository.findByName("ROLE_ADMIN").isEmpty()) {
            roleRepository.save(Role.builder().name("ROLE_ADMIN").description("Administrator").isActive(true).build());
        }
        if (roleRepository.findByName("ROLE_USER").isEmpty()) {
            roleRepository.save(Role.builder().name("ROLE_USER").description("Standard User").isActive(true).build());
        }
        if (roleRepository.findByName("ROLE_MANAGER").isEmpty()) {
            roleRepository.save(Role.builder().name("ROLE_MANAGER").description("Manager").isActive(true).build());
        }
        if (roleRepository.findByName("ROLE_OPERATOR").isEmpty()) {
            roleRepository.save(Role.builder().name("ROLE_OPERATOR").description("Operator").isActive(true).build());
        }
        if (roleRepository.findByName("ROLE_IT_ADMIN").isEmpty()) {
            roleRepository.save(Role.builder().name("ROLE_IT_ADMIN").description("IT Administrator").isActive(true).build());
        }
        if (roleRepository.findByName("ROLE_PLANT_HOD").isEmpty()) {
            roleRepository.save(Role.builder().name("ROLE_PLANT_HOD").description("Plant Hod").isActive(true).build());
        }
    }
}
