package com.secureauth.productservice.service.impl;

import com.secureauth.productservice.dto.InhouseCalibrationMachineRequest;
import com.secureauth.productservice.dto.InhouseCalibrationMachineResponse;
import com.secureauth.productservice.entity.GageSubType;
import com.secureauth.productservice.entity.GageType;
import com.secureauth.productservice.entity.InhouseCalibrationMachine;
import com.secureauth.productservice.repository.GageSubTypeRepository;
import com.secureauth.productservice.repository.GageTypeRepository;
import com.secureauth.productservice.repository.InhouseCalibrationMachineRepository;
import com.secureauth.productservice.service.InhouseCalibrationMachineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class InhouseCalibrationMachineServiceImpl implements InhouseCalibrationMachineService {

    @Autowired
    private InhouseCalibrationMachineRepository inhouseCalibrationMachineRepository;

    @Autowired
    private GageTypeRepository gageTypeRepository;

    @Autowired
    private GageSubTypeRepository gageSubTypeRepository;

    @Override
    public InhouseCalibrationMachineResponse createInhouseCalibrationMachine(InhouseCalibrationMachineRequest request) {
        // Check if machine name is unique
        if (inhouseCalibrationMachineRepository.existsByMachineName(request.getMachineName())) {
            throw new RuntimeException("Machine name already exists: " + request.getMachineName());
        }

        // Check if instrument code is unique
        if (inhouseCalibrationMachineRepository.existsByInstrumentCode(request.getInstrumentCode())) {
            throw new RuntimeException("Instrument code already exists: " + request.getInstrumentCode());
        }

        // Check if machine equipment number is unique
        if (inhouseCalibrationMachineRepository.existsByMachineEquipmentNumber(request.getMachineEquipmentNumber())) {
            throw new RuntimeException("Machine equipment number already exists: " + request.getMachineEquipmentNumber());
        }

        GageType gageType = gageTypeRepository.findById(request.getGageTypeId())
                .orElseThrow(() -> new RuntimeException("GageType not found with id: " + request.getGageTypeId()));

        GageSubType gageSubType = gageSubTypeRepository.findById(request.getGageSubTypeId())
                .orElseThrow(() -> new RuntimeException("GageSubType not found with id: " + request.getGageSubTypeId()));

        InhouseCalibrationMachine machine = InhouseCalibrationMachine.builder()
                .machineName(request.getMachineName())
                .instrumentName(request.getInstrumentName())
                .instrumentCode(request.getInstrumentCode())
                .accuracy(request.getAccuracy())
                .resolution(request.getResolution())
                .location(request.getLocation())
                .status(request.getStatus())
                .manufacturer(request.getManufacturer())
                .machineEquipmentNumber(request.getMachineEquipmentNumber())
                .guaranteeExpiryDate(request.getGuaranteeExpiryDate())
                .gageType(gageType)
                .gageSubType(gageSubType)
                .build();

        machine = inhouseCalibrationMachineRepository.save(machine);
        return mapToResponse(machine);
    }

    @Override
    public InhouseCalibrationMachineResponse getInhouseCalibrationMachineById(Long id) {
        InhouseCalibrationMachine machine = inhouseCalibrationMachineRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("InhouseCalibrationMachine not found with id: " + id));
        return mapToResponse(machine);
    }

    @Override
    public List<InhouseCalibrationMachineResponse> getAllInhouseCalibrationMachines() {
        return inhouseCalibrationMachineRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public InhouseCalibrationMachineResponse updateInhouseCalibrationMachine(Long id, InhouseCalibrationMachineRequest request) {
        InhouseCalibrationMachine machine = inhouseCalibrationMachineRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("InhouseCalibrationMachine not found with id: " + id));

        // Check if machine name is unique (excluding current machine)
        if (inhouseCalibrationMachineRepository.existsByMachineNameAndIdNot(request.getMachineName(), id)) {
            throw new RuntimeException("Machine name already exists: " + request.getMachineName());
        }

        // Check if instrument code is unique (excluding current machine)
        if (inhouseCalibrationMachineRepository.existsByInstrumentCodeAndIdNot(request.getInstrumentCode(), id)) {
            throw new RuntimeException("Instrument code already exists: " + request.getInstrumentCode());
        }

        // Check if machine equipment number is unique (excluding current machine)
        if (inhouseCalibrationMachineRepository.existsByMachineEquipmentNumberAndIdNot(request.getMachineEquipmentNumber(), id)) {
            throw new RuntimeException("Machine equipment number already exists: " + request.getMachineEquipmentNumber());
        }

        GageType gageType = gageTypeRepository.findById(request.getGageTypeId())
                .orElseThrow(() -> new RuntimeException("GageType not found with id: " + request.getGageTypeId()));

        GageSubType gageSubType = gageSubTypeRepository.findById(request.getGageSubTypeId())
                .orElseThrow(() -> new RuntimeException("GageSubType not found with id: " + request.getGageSubTypeId()));

        machine.setMachineName(request.getMachineName());
        machine.setInstrumentName(request.getInstrumentName());
        machine.setInstrumentCode(request.getInstrumentCode());
        machine.setAccuracy(request.getAccuracy());
        machine.setResolution(request.getResolution());
        machine.setLocation(request.getLocation());
        machine.setStatus(request.getStatus());
        machine.setManufacturer(request.getManufacturer());
        machine.setMachineEquipmentNumber(request.getMachineEquipmentNumber());
        machine.setGuaranteeExpiryDate(request.getGuaranteeExpiryDate());
        machine.setGageType(gageType);
        machine.setGageSubType(gageSubType);

        machine = inhouseCalibrationMachineRepository.save(machine);
        return mapToResponse(machine);
    }

    @Override
    public void deleteInhouseCalibrationMachine(Long id) {
        if (!inhouseCalibrationMachineRepository.existsById(id)) {
            throw new RuntimeException("InhouseCalibrationMachine not found with id: " + id);
        }
        inhouseCalibrationMachineRepository.deleteById(id);
    }

    @Override
    public boolean isMachineNameUnique(String machineName) {
        return !inhouseCalibrationMachineRepository.existsByMachineName(machineName);
    }

    @Override
    public boolean isInstrumentCodeUnique(String instrumentCode) {
        return !inhouseCalibrationMachineRepository.existsByInstrumentCode(instrumentCode);
    }

    @Override
    public boolean isMachineEquipmentNumberUnique(String machineEquipmentNumber) {
        return !inhouseCalibrationMachineRepository.existsByMachineEquipmentNumber(machineEquipmentNumber);
    }

    private InhouseCalibrationMachineResponse mapToResponse(InhouseCalibrationMachine machine) {
        return InhouseCalibrationMachineResponse.builder()
                .id(machine.getId())
                .machineName(machine.getMachineName())
                .instrumentName(machine.getInstrumentName())
                .instrumentCode(machine.getInstrumentCode())
                .accuracy(machine.getAccuracy())
                .resolution(machine.getResolution())
                .location(machine.getLocation())
                .status(machine.getStatus())
                .manufacturer(machine.getManufacturer())
                .machineEquipmentNumber(machine.getMachineEquipmentNumber())
                .guaranteeExpiryDate(machine.getGuaranteeExpiryDate())
                .gageTypeId(machine.getGageType().getId())
                .gageTypeName(machine.getGageType().getName())
                .gageSubTypeId(machine.getGageSubType().getId())
                .gageSubTypeName(machine.getGageSubType().getName())
                .build();
    }
}