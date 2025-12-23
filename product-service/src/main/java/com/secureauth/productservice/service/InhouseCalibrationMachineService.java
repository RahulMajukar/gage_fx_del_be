package com.secureauth.productservice.service;

import com.secureauth.productservice.dto.InhouseCalibrationMachineRequest;
import com.secureauth.productservice.dto.InhouseCalibrationMachineResponse;

import java.util.List;

public interface InhouseCalibrationMachineService {

    InhouseCalibrationMachineResponse createInhouseCalibrationMachine(InhouseCalibrationMachineRequest request);

    InhouseCalibrationMachineResponse getInhouseCalibrationMachineById(Long id);

    List<InhouseCalibrationMachineResponse> getAllInhouseCalibrationMachines();

    InhouseCalibrationMachineResponse updateInhouseCalibrationMachine(Long id, InhouseCalibrationMachineRequest request);

    void deleteInhouseCalibrationMachine(Long id);

    boolean isMachineNameUnique(String machineName);

    boolean isInstrumentCodeUnique(String instrumentCode);

    boolean isMachineEquipmentNumberUnique(String machineEquipmentNumber);
}