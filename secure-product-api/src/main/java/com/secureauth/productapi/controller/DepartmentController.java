package com.secureauth.productapi.controller;

import com.secureauth.productapi.entity.Department;
import com.secureauth.productapi.repository.DepartmentRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/departments")
public class DepartmentController {

    @Autowired private DepartmentRepository departmentRepository;

    @GetMapping
    public List<Department> list() {
        return departmentRepository.findAll();
    }

    @PostMapping
    public Department create(@Valid @RequestBody Department department) {
        return departmentRepository.save(department);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Department> update(@PathVariable Long id, @RequestBody Department incoming) {
        return departmentRepository.findById(id)
                .map(d -> {
                    d.setName(incoming.getName());
                    d.setType(incoming.getType());
                    d.setContactPerson(incoming.getContactPerson());
                    d.setContactEmail(incoming.getContactEmail());
                    d.setContactPhone(incoming.getContactPhone());
                    d.setCostCenter(incoming.getCostCenter());
                    d.setBudget(incoming.getBudget());
                    d.setIsActive(incoming.getIsActive());
                    return ResponseEntity.ok(departmentRepository.save(d));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<?> toggleStatus(@PathVariable Long id, @RequestParam boolean active) {
        return departmentRepository.findById(id)
                .map(d -> {
                    d.setIsActive(active);
                    departmentRepository.save(d);
                    return ResponseEntity.ok().build();
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}


