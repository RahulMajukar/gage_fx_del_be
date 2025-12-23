package com.secureauth.productapi.controller;

import com.secureauth.productapi.entity.Function;
import com.secureauth.productapi.repository.FunctionRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/functions")
public class FunctionController {

    @Autowired private FunctionRepository functionRepository;

    @GetMapping
    public List<Function> list() {
        return functionRepository.findAll();
    }

    @PostMapping
    public Function create(@Valid @RequestBody Function function) {
        return functionRepository.save(function);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Function> update(@PathVariable Long id, @RequestBody Function incoming) {
        return functionRepository.findById(id)
                .map(f -> {
                    f.setName(incoming.getName());
                    f.setCode(incoming.getCode());
                    f.setDescription(incoming.getDescription());
                    f.setSortOrder(incoming.getSortOrder());
                    f.setIsCritical(incoming.getIsCritical());
                    f.setIsActive(incoming.getIsActive());
                    return ResponseEntity.ok(functionRepository.save(f));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<?> toggleStatus(@PathVariable Long id, @RequestParam boolean active) {
        return functionRepository.findById(id)
                .map(f -> {
                    f.setIsActive(active);
                    functionRepository.save(f);
                    return ResponseEntity.ok().build();
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}


