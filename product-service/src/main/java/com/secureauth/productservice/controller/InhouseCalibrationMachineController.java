package com.secureauth.productservice.controller;

import com.secureauth.productservice.dto.InhouseCalibrationMachineRequest;
import com.secureauth.productservice.dto.InhouseCalibrationMachineResponse;
import com.secureauth.productservice.service.InhouseCalibrationMachineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/inhouse-calibration-machines")
public class InhouseCalibrationMachineController {

    @Autowired
    private InhouseCalibrationMachineService inhouseCalibrationMachineService;

    @PostMapping("/add")
    public ResponseEntity<InhouseCalibrationMachineResponse> createInhouseCalibrationMachine(@Valid @RequestBody InhouseCalibrationMachineRequest request) {
        return ResponseEntity.ok(inhouseCalibrationMachineService.createInhouseCalibrationMachine(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<InhouseCalibrationMachineResponse> getInhouseCalibrationMachineById(@PathVariable Long id) {
        return ResponseEntity.ok(inhouseCalibrationMachineService.getInhouseCalibrationMachineById(id));
    }

    @GetMapping("/all")
    public ResponseEntity<List<InhouseCalibrationMachineResponse>> getAllInhouseCalibrationMachines() {
        return ResponseEntity.ok(inhouseCalibrationMachineService.getAllInhouseCalibrationMachines());
    }

    @PutMapping("/{id}")
    public ResponseEntity<InhouseCalibrationMachineResponse> updateInhouseCalibrationMachine(@PathVariable Long id,
                                                                                             @Valid @RequestBody InhouseCalibrationMachineRequest request) {
        return ResponseEntity.ok(inhouseCalibrationMachineService.updateInhouseCalibrationMachine(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteInhouseCalibrationMachine(@PathVariable Long id) {
        inhouseCalibrationMachineService.deleteInhouseCalibrationMachine(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/validate/machine-name")
    public ResponseEntity<Boolean> isMachineNameUnique(@RequestParam String machineName) {
        return ResponseEntity.ok(inhouseCalibrationMachineService.isMachineNameUnique(machineName));
    }

    @GetMapping("/validate/instrument-code")
    public ResponseEntity<Boolean> isInstrumentCodeUnique(@RequestParam String instrumentCode) {
        return ResponseEntity.ok(inhouseCalibrationMachineService.isInstrumentCodeUnique(instrumentCode));
    }

    @GetMapping("/validate/equipment-number")
    public ResponseEntity<Boolean> isMachineEquipmentNumberUnique(@RequestParam String machineEquipmentNumber) {
        return ResponseEntity.ok(inhouseCalibrationMachineService.isMachineEquipmentNumberUnique(machineEquipmentNumber));
    }
}