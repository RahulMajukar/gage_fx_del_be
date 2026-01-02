// src/main/java/com/secureauth/productservice/controller/CalibrationLabTechHistoryController.java
package com.secureauth.productservice.controller;

import com.secureauth.productservice.entity.*;
import com.secureauth.productservice.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/lab-calibration-history")
@RequiredArgsConstructor
public class CalibrationLabTechHistoryController {

    private final CalibrationLabTechHistoryRepository historyRepository;
    private final GageRepository gageRepository;
    private final InhouseCalibrationMachineRepository machineRepository;

    // CREATE
    @PostMapping("/lab-calibration-history")
    public ResponseEntity<CalibrationLabTechHistory> create(
            @RequestBody CalibrationLabTechHistory request) {

        Gage gage = gageRepository.findById(request.getGage().getId())
                .orElseThrow(() -> new RuntimeException("Gage not found"));

        request.setGage(gage);

        if (request.getCalibrationMachine() != null &&
                request.getCalibrationMachine().getId() != null) {

            InhouseCalibrationMachine machine = machineRepository.findById(request.getCalibrationMachine().getId())
                    .orElseThrow(() -> new RuntimeException("Machine not found"));

            request.setCalibrationMachine(machine);
        }

        return ResponseEntity.ok(historyRepository.save(request));
    }

    // GET BY ID
    @GetMapping("/lab-calibration-history/{id}")
    public ResponseEntity<CalibrationLabTechHistory> getById(@PathVariable Long id) {
        return ResponseEntity.ok(
                historyRepository.findById(id)
                        .orElseThrow(() -> new RuntimeException("History not found")));
    }

    // GET BY GAGE
    @GetMapping("/lab-calibration-history/gage/{gageId}")
    public ResponseEntity<List<CalibrationLabTechHistory>> getByGage(
            @PathVariable Long gageId) {
        return ResponseEntity.ok(historyRepository.findByGage_Id(gageId));
    }

    // GET ALL
    @GetMapping("/lab-calibration-history")
    public ResponseEntity<List<CalibrationLabTechHistory>> getAll() {
        return ResponseEntity.ok(historyRepository.findAll());
    }

    // DELETE
    @DeleteMapping("/lab-calibration-history/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        historyRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
