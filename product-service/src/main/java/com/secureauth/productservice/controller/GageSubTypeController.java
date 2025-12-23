package com.secureauth.productservice.controller;

import com.secureauth.productservice.dto.GageSubTypeRequest;
import com.secureauth.productservice.dto.GageSubTypeResponse;
import com.secureauth.productservice.service.GageSubTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/gage-sub-types")
public class GageSubTypeController {

    @Autowired
    private GageSubTypeService gageSubTypeService;

    @PostMapping("/add")
    public ResponseEntity<GageSubTypeResponse> createGageSubType(@Valid @RequestBody GageSubTypeRequest request) {
        return ResponseEntity.ok(gageSubTypeService.createGageSubType(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<GageSubTypeResponse> getGageSubTypeById(@PathVariable Long id) {
        return ResponseEntity.ok(gageSubTypeService.getGageSubTypeById(id));
    }

    @GetMapping("/all")
    public ResponseEntity<List<GageSubTypeResponse>> getAllGageSubTypes() {
        return ResponseEntity.ok(gageSubTypeService.getAllGageSubTypes());
    }

    @PutMapping("/{id}")
    public ResponseEntity<GageSubTypeResponse> updateGageSubType(@PathVariable Long id,
                                                                  @Valid @RequestBody GageSubTypeRequest request) {
        return ResponseEntity.ok(gageSubTypeService.updateGageSubType(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGageSubType(@PathVariable Long id) {
        gageSubTypeService.deleteGageSubType(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/validate/name")
    public ResponseEntity<Boolean> isNameUnique(@RequestParam String name) {
        return ResponseEntity.ok(gageSubTypeService.isNameUnique(name));
    }
}

