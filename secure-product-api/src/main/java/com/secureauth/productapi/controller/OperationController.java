package com.secureauth.productapi.controller;

import com.secureauth.productapi.entity.Operation;
import com.secureauth.productapi.repository.OperationRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/operations")
public class OperationController {

    @Autowired private OperationRepository operationRepository;

    @GetMapping
    public List<Operation> list() {
        return operationRepository.findAll();
    }

    @PostMapping
    public Operation create(@Valid @RequestBody Operation operation) {
        return operationRepository.save(operation);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Operation> update(@PathVariable Long id, @RequestBody Operation incoming) {
        return operationRepository.findById(id)
                .map(o -> {
                    o.setName(incoming.getName());
                    o.setCode(incoming.getCode());
                    o.setDescription(incoming.getDescription());
                    o.setEstimatedTimeMin(incoming.getEstimatedTimeMin());
                    o.setRequiredSkills(incoming.getRequiredSkills());
                    o.setIsMandatory(incoming.getIsMandatory());
                    o.setIsActive(incoming.getIsActive());
                    return ResponseEntity.ok(operationRepository.save(o));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<?> toggleStatus(@PathVariable Long id, @RequestParam boolean active) {
        return operationRepository.findById(id)
                .map(o -> {
                    o.setIsActive(active);
                    operationRepository.save(o);
                    return ResponseEntity.ok().build();
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}


