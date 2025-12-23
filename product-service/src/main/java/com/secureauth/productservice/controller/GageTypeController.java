package com.secureauth.productservice.controller;

import com.secureauth.productservice.dto.GageTypeRequest;
import com.secureauth.productservice.dto.GageTypeResponse;
import com.secureauth.productservice.service.GageTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/gage-types")
public class GageTypeController {

    @Autowired
    private GageTypeService gageTypeService;

    @PostMapping("/add")
    public ResponseEntity<GageTypeResponse> createGageType(@Valid @RequestBody GageTypeRequest request) {
        return ResponseEntity.ok(gageTypeService.createGageType(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<GageTypeResponse> getGageTypeById(@PathVariable Long id) {
        return ResponseEntity.ok(gageTypeService.getGageTypeById(id));
    }

    @GetMapping("/all")
    public ResponseEntity<List<GageTypeResponse>> getAllGageTypes() {
        return ResponseEntity.ok(gageTypeService.getAllGageTypes());
    }

    @PutMapping("/{id}")
    public ResponseEntity<GageTypeResponse> updateGageType(@PathVariable Long id,
                                                           @Valid @RequestBody GageTypeRequest request) {
        return ResponseEntity.ok(gageTypeService.updateGageType(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGageType(@PathVariable Long id) {
        gageTypeService.deleteGageType(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/validate/name")
    public ResponseEntity<Boolean> isNameUnique(@RequestParam String name) {
        return ResponseEntity.ok(gageTypeService.isNameUnique(name));
    }
}
