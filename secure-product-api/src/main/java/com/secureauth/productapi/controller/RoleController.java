package com.secureauth.productapi.controller;

import com.secureauth.productapi.entity.Role;
import com.secureauth.productapi.repository.RoleRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/roles")
public class RoleController {

    @Autowired private RoleRepository roleRepository;

    @GetMapping
    public List<Role> list() {
        return roleRepository.findAll();
    }

    @PostMapping
    public Role create(@Valid @RequestBody Role role) {
        return roleRepository.save(role);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Role> update(@PathVariable Long id, @RequestBody Role incoming) {
        return roleRepository.findById(id)
                .map(r -> {
                    r.setName(incoming.getName());
                    r.setDescription(incoming.getDescription());
                    r.setIsActive(incoming.getIsActive());
                    return ResponseEntity.ok(roleRepository.save(r));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<?> toggleStatus(@PathVariable Long id, @RequestParam boolean active) {
        return roleRepository.findById(id)
                .map(r -> {
                    r.setIsActive(active);
                    roleRepository.save(r);
                    return ResponseEntity.ok().build();
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}


